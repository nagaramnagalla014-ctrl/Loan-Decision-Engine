package com.loan.decisionengine.service;

import com.loan.decisionengine.exception.LoanDecisionException;
import com.loan.decisionengine.model.LoanDecision;
import com.loan.decisionengine.repository.LoanDecisionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanDecisionService {

    @Autowired private LoanDecisionRepository decisionRepository;

    public LoanDecision getByApplicationId(Long applicationId) {
        return decisionRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new LoanDecisionException("Decision not found for application: " + applicationId));
    }

    public LoanDecision getByApplicationNumber(String appNumber) {
        return decisionRepository.findByApplicationNumber(appNumber)
                .orElseThrow(() -> new LoanDecisionException("Decision not found for: " + appNumber));
    }

    public List<LoanDecision> getAll() {
        return decisionRepository.findAll();
    }

    public List<LoanDecision> getByDecision(LoanDecision.Decision decision) {
        return decisionRepository.findByDecisionOrderByDecidedAtDesc(decision);
    }
}
