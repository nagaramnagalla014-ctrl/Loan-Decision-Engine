package com.loan.decisionengine.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications")
public class LoanApplication {

    public enum LoanType { PERSONAL, MORTGAGE, AUTO, BUSINESS }
    public enum EmploymentType { EMPLOYED, SELF_EMPLOYED, RETIRED, UNEMPLOYED }
    public enum ApplicationStatus { SUBMITTED, PROCESSING, APPROVED, REJECTED, MANUAL_REVIEW, ERROR }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String applicationNumber;

    private Long applicantId;
    private String applicantEmail;
    private String firstName;
    private String lastName;
    private String phone;

    @Enumerated(EnumType.STRING)
    private LoanType loanType;

    @Column(precision = 15, scale = 2)
    private BigDecimal requestedAmount;

    private Integer termMonths;

    @Column(precision = 15, scale = 2)
    private BigDecimal annualIncome;

    @Column(precision = 15, scale = 2)
    private BigDecimal monthlyDebtPayments;

    private Integer creditScore;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    private Integer yearsAtCurrentJob;

    @Column(precision = 15, scale = 2)
    private BigDecimal collateralValue;

    @Column(length = 1000)
    private String loanPurpose;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Column(precision = 6, scale = 2)
    private BigDecimal debtToIncomeRatio;

    @Column(precision = 6, scale = 2)
    private BigDecimal loanToIncomeRatio;

    @Column(precision = 6, scale = 2)
    private BigDecimal loanToValueRatio;

    private String rulesEngineVersion;

    private LocalDateTime submittedAt;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getApplicationNumber() { return applicationNumber; }
    public void setApplicationNumber(String applicationNumber) { this.applicationNumber = applicationNumber; }
    public Long getApplicantId() { return applicantId; }
    public void setApplicantId(Long applicantId) { this.applicantId = applicantId; }
    public String getApplicantEmail() { return applicantEmail; }
    public void setApplicantEmail(String applicantEmail) { this.applicantEmail = applicantEmail; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LoanType getLoanType() { return loanType; }
    public void setLoanType(LoanType loanType) { this.loanType = loanType; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }
    public Integer getTermMonths() { return termMonths; }
    public void setTermMonths(Integer termMonths) { this.termMonths = termMonths; }
    public BigDecimal getAnnualIncome() { return annualIncome; }
    public void setAnnualIncome(BigDecimal annualIncome) { this.annualIncome = annualIncome; }
    public BigDecimal getMonthlyDebtPayments() { return monthlyDebtPayments; }
    public void setMonthlyDebtPayments(BigDecimal monthlyDebtPayments) { this.monthlyDebtPayments = monthlyDebtPayments; }
    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }
    public EmploymentType getEmploymentType() { return employmentType; }
    public void setEmploymentType(EmploymentType employmentType) { this.employmentType = employmentType; }
    public Integer getYearsAtCurrentJob() { return yearsAtCurrentJob; }
    public void setYearsAtCurrentJob(Integer yearsAtCurrentJob) { this.yearsAtCurrentJob = yearsAtCurrentJob; }
    public BigDecimal getCollateralValue() { return collateralValue; }
    public void setCollateralValue(BigDecimal collateralValue) { this.collateralValue = collateralValue; }
    public String getLoanPurpose() { return loanPurpose; }
    public void setLoanPurpose(String loanPurpose) { this.loanPurpose = loanPurpose; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public BigDecimal getDebtToIncomeRatio() { return debtToIncomeRatio; }
    public void setDebtToIncomeRatio(BigDecimal debtToIncomeRatio) { this.debtToIncomeRatio = debtToIncomeRatio; }
    public BigDecimal getLoanToIncomeRatio() { return loanToIncomeRatio; }
    public void setLoanToIncomeRatio(BigDecimal loanToIncomeRatio) { this.loanToIncomeRatio = loanToIncomeRatio; }
    public BigDecimal getLoanToValueRatio() { return loanToValueRatio; }
    public void setLoanToValueRatio(BigDecimal loanToValueRatio) { this.loanToValueRatio = loanToValueRatio; }
    public String getRulesEngineVersion() { return rulesEngineVersion; }
    public void setRulesEngineVersion(String rulesEngineVersion) { this.rulesEngineVersion = rulesEngineVersion; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
