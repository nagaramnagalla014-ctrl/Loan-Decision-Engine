package com.loan.decisionengine.repository;

import com.loan.decisionengine.model.LoanDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LoanDecisionRepository extends JpaRepository<LoanDecision, Long> {
    Optional<LoanDecision> findByApplicationId(Long applicationId);
    Optional<LoanDecision> findByApplicationNumber(String applicationNumber);
    List<LoanDecision> findByDecisionOrderByDecidedAtDesc(LoanDecision.Decision decision);
    long countByDecision(LoanDecision.Decision decision);
}
