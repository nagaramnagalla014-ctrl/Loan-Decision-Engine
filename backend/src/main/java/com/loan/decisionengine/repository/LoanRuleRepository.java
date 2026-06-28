package com.loan.decisionengine.repository;

import com.loan.decisionengine.model.LoanRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LoanRuleRepository extends JpaRepository<LoanRule, Long> {
    List<LoanRule> findByActiveTrueOrderBySalienceDesc();
    List<LoanRule> findByCategory(LoanRule.RuleCategory category);
    Optional<LoanRule> findByRuleName(String ruleName);
    boolean existsByRuleName(String ruleName);
}
