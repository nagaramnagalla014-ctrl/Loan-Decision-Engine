package com.loan.decisionengine.service;

import com.loan.decisionengine.drools.DroolsRuleService;
import com.loan.decisionengine.drools.facts.DecisionResult;
import com.loan.decisionengine.drools.facts.LoanApplicationFact;
import com.loan.decisionengine.dto.LoanApplicationRequest;
import com.loan.decisionengine.dto.ManualReviewRequest;
import com.loan.decisionengine.exception.LoanDecisionException;
import com.loan.decisionengine.kafka.LoanApplicationEvent;
import com.loan.decisionengine.kafka.LoanEventProducer;
import com.loan.decisionengine.model.LoanApplication;
import com.loan.decisionengine.model.LoanDecision;
import com.loan.decisionengine.model.User;
import com.loan.decisionengine.repository.LoanApplicationRepository;
import com.loan.decisionengine.repository.LoanDecisionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class LoanApplicationService {

    @Autowired private LoanApplicationRepository applicationRepository;
    @Autowired private LoanDecisionRepository decisionRepository;
    @Autowired private DroolsRuleService droolsRuleService;
    @Autowired private LoanEventProducer eventProducer;
    @Autowired private RedisService redisService;

    @Transactional
    public LoanApplication submit(LoanApplicationRequest req, User applicant) {
        LoanApplication app = new LoanApplication();
        app.setApplicationNumber(generateAppNumber());
        app.setApplicantId(applicant.getId());
        app.setApplicantEmail(applicant.getEmail());
        app.setFirstName(req.getFirstName());
        app.setLastName(req.getLastName());
        app.setPhone(req.getPhone());
        app.setLoanType(LoanApplication.LoanType.valueOf(req.getLoanType()));
        app.setRequestedAmount(req.getRequestedAmount());
        app.setTermMonths(req.getTermMonths());
        app.setAnnualIncome(req.getAnnualIncome());
        app.setMonthlyDebtPayments(req.getMonthlyDebtPayments());
        app.setCreditScore(req.getCreditScore());
        app.setEmploymentType(LoanApplication.EmploymentType.valueOf(req.getEmploymentType()));
        app.setYearsAtCurrentJob(req.getYearsAtCurrentJob() != null ? req.getYearsAtCurrentJob() : 0);
        app.setCollateralValue(req.getCollateralValue() != null ? req.getCollateralValue() : BigDecimal.ZERO);
        app.setLoanPurpose(req.getLoanPurpose());
        app.setStatus(LoanApplication.ApplicationStatus.PROCESSING);
        app.setSubmittedAt(LocalDateTime.now());
        app.setRulesEngineVersion(droolsRuleService.getCurrentEngineVersion());

        // Compute financial ratios
        double annualInc = req.getAnnualIncome().doubleValue();
        double monthlyInc = annualInc / 12.0;
        double monthlyDebt = req.getMonthlyDebtPayments().doubleValue();
        double reqAmt = req.getRequestedAmount().doubleValue();
        double collateral = app.getCollateralValue().doubleValue();

        app.setDebtToIncomeRatio(monthlyInc > 0
                ? bd((monthlyDebt / monthlyInc) * 100) : bd(100));
        app.setLoanToIncomeRatio(annualInc > 0
                ? bd(reqAmt / annualInc) : bd(999));
        app.setLoanToValueRatio(collateral > 0
                ? bd((reqAmt / collateral) * 100) : BigDecimal.ZERO);

        LoanApplication saved = applicationRepository.save(app);

        // Evaluate via Drools
        LoanApplicationFact fact = new LoanApplicationFact(
                saved.getId(), req.getLoanType(), reqAmt, req.getTermMonths(),
                annualInc, monthlyDebt, req.getCreditScore(), req.getEmploymentType(),
                app.getYearsAtCurrentJob(), collateral);

        try {
            DecisionResult result = droolsRuleService.evaluate(fact);
            persistDecision(saved, result);
        } catch (Exception e) {
            saved.setStatus(LoanApplication.ApplicationStatus.ERROR);
            applicationRepository.save(saved);
            throw new LoanDecisionException("Rules engine evaluation failed: " + e.getMessage());
        }

        eventProducer.sendApplicationSubmitted(LoanApplicationEvent.submitted(
                saved.getId(), saved.getApplicationNumber(),
                applicant.getEmail(), req.getLoanType(), reqAmt));

        return applicationRepository.findById(saved.getId()).orElse(saved);
    }

    private void persistDecision(LoanApplication app, DecisionResult result) {
        LoanDecision.Decision decision = LoanDecision.Decision.valueOf(result.getDecision());
        LoanDecision.RiskTier riskTier = LoanDecision.RiskTier.valueOf(
                result.getRiskTier().isEmpty() ? "MEDIUM" : result.getRiskTier());

        LoanDecision ld = new LoanDecision();
        ld.setApplicationId(app.getId());
        ld.setApplicationNumber(app.getApplicationNumber());
        ld.setDecision(decision);
        ld.setRiskScore(result.getRiskScore());
        ld.setRiskTier(riskTier);
        ld.setDecisionReasons(String.join(" | ", result.getReasons()));
        ld.setRulesEngineVersion(droolsRuleService.getCurrentEngineVersion());
        ld.setDecidedAt(LocalDateTime.now());

        if (decision == LoanDecision.Decision.APPROVED) {
            ld.setApprovedAmount(BigDecimal.valueOf(result.getApprovedAmount()).setScale(2, RoundingMode.HALF_UP));
            ld.setInterestRate(BigDecimal.valueOf(result.getInterestRate()).setScale(2, RoundingMode.HALF_UP));
            ld.setApprovedTermMonths(result.getApprovedTermMonths() > 0
                    ? result.getApprovedTermMonths() : app.getTermMonths());
        }

        decisionRepository.save(ld);

        app.setStatus(LoanApplication.ApplicationStatus.valueOf(result.getDecision()));
        app.setProcessedAt(LocalDateTime.now());
        applicationRepository.save(app);

        redisService.cacheApplicationStatus(app.getApplicationNumber(), result.getDecision());

        eventProducer.sendDecisionCompleted(LoanApplicationEvent.decided(
                app.getId(), app.getApplicationNumber(), app.getApplicantEmail(),
                result.getDecision(), droolsRuleService.getCurrentEngineVersion()));
    }

    @Transactional
    public LoanDecision manualReview(Long appId, ManualReviewRequest req, User reviewer) {
        LoanApplication app = applicationRepository.findById(appId)
                .orElseThrow(() -> new LoanDecisionException("Application not found: " + appId));

        if (app.getStatus() != LoanApplication.ApplicationStatus.MANUAL_REVIEW) {
            throw new LoanDecisionException("Application is not in MANUAL_REVIEW status");
        }
        if (!"APPROVED".equalsIgnoreCase(req.getDecision()) &&
                !"REJECTED".equalsIgnoreCase(req.getDecision())) {
            throw new LoanDecisionException("Decision must be APPROVED or REJECTED");
        }

        LoanDecision ld = decisionRepository.findByApplicationId(appId)
                .orElseThrow(() -> new LoanDecisionException("Decision record not found"));

        LoanDecision.Decision decision = LoanDecision.Decision.valueOf(req.getDecision().toUpperCase());
        ld.setDecision(decision);
        ld.setReviewedBy(reviewer.getEmail());
        ld.setReviewNotes(req.getReviewNotes());
        ld.setReviewedAt(LocalDateTime.now());

        if (decision == LoanDecision.Decision.APPROVED) {
            if (req.getApprovedAmount() != null) ld.setApprovedAmount(req.getApprovedAmount());
            if (req.getInterestRate() != null) ld.setInterestRate(req.getInterestRate());
            if (req.getApprovedTermMonths() != null) ld.setApprovedTermMonths(req.getApprovedTermMonths());
        }

        decisionRepository.save(ld);

        app.setStatus(LoanApplication.ApplicationStatus.valueOf(decision.name()));
        app.setProcessedAt(LocalDateTime.now());
        applicationRepository.save(app);

        redisService.cacheApplicationStatus(app.getApplicationNumber(), decision.name());
        return ld;
    }

    public List<LoanApplication> getMyApplications(Long applicantId) {
        return applicationRepository.findByApplicantIdOrderByCreatedAtDesc(applicantId);
    }

    public LoanApplication getById(Long id, User requester) {
        LoanApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new LoanDecisionException("Application not found: " + id));
        if (requester.getRole() == User.Role.APPLICANT &&
                !app.getApplicantId().equals(requester.getId())) {
            throw new LoanDecisionException("Access denied");
        }
        return app;
    }

    public LoanApplication getByApplicationNumber(String appNumber) {
        return applicationRepository.findByApplicationNumber(appNumber)
                .orElseThrow(() -> new LoanDecisionException("Application not found: " + appNumber));
    }

    public List<LoanApplication> getPendingManualReview() {
        return applicationRepository.findPendingManualReview();
    }

    private String generateAppNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "APP-" + date + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private BigDecimal bd(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP);
    }
}
