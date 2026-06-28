package com.loan.decisionengine.config;

import com.loan.decisionengine.model.LoanRule;
import com.loan.decisionengine.model.User;
import com.loan.decisionengine.repository.LoanRuleRepository;
import com.loan.decisionengine.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired private UserRepository userRepository;
    @Autowired private LoanRuleRepository ruleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedRules();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;
        List<User> users = List.of(
            user("john.smith@loanengine.com",  "password123", "John",   "Smith",   User.Role.APPLICANT),
            user("jane.doe@loanengine.com",    "password123", "Jane",   "Doe",     User.Role.APPLICANT),
            user("analyst@loanengine.com",     "admin123",   "Alex",   "Turner",  User.Role.ANALYST),
            user("rules@loanengine.com",       "admin123",   "Morgan", "Lee",     User.Role.BUSINESS_ADMIN),
            user("admin@loanengine.com",       "admin123",   "Admin",  "User",    User.Role.ADMIN)
        );
        userRepository.saveAll(users);
        log.info("Seeded {} users", users.size());
    }

    private User user(String email, String pwd, String first, String last, User.Role role) {
        User u = new User();
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(pwd));
        u.setFirstName(first);
        u.setLastName(last);
        u.setRole(role);
        return u;
    }

    private void seedRules() {
        if (ruleRepository.count() > 0) return;

        String pkg = "package com.loan.decisionengine.rules;\n\n" +
                "import com.loan.decisionengine.drools.facts.LoanApplicationFact;\n" +
                "import com.loan.decisionengine.drools.facts.DecisionResult;\n\n";

        List<LoanRule> rules = List.of(
            rule("reject_poor_credit", "Reject applications with credit score below 580",
                    LoanRule.RuleCategory.CREDIT_SCORE, 100, true, pkg +
                    "rule \"Reject - Poor Credit Score\"\n" +
                    "    salience 100\n" +
                    "    when\n" +
                    "        $fact : LoanApplicationFact( creditScore < 580 )\n" +
                    "        $result : DecisionResult( decision == null || decision.isEmpty() )\n" +
                    "    then\n" +
                    "        $result.setDecision(\"REJECTED\");\n" +
                    "        $result.setRiskTier(\"VERY_HIGH\");\n" +
                    "        $result.setRiskScore(95.0);\n" +
                    "        $result.addReason(\"Credit score of \" + $fact.getCreditScore() + \" is below the minimum threshold of 580\");\n" +
                    "end\n"),

            rule("reject_unemployed", "Reject unemployed applicants with no income stability",
                    LoanRule.RuleCategory.EMPLOYMENT, 95, true, pkg +
                    "rule \"Reject - Unemployed Applicant\"\n" +
                    "    salience 95\n" +
                    "    when\n" +
                    "        $fact : LoanApplicationFact( employmentType == \"UNEMPLOYED\" )\n" +
                    "        $result : DecisionResult( decision == null || decision.isEmpty() )\n" +
                    "    then\n" +
                    "        $result.setDecision(\"REJECTED\");\n" +
                    "        $result.setRiskTier(\"VERY_HIGH\");\n" +
                    "        $result.setRiskScore(92.0);\n" +
                    "        $result.addReason(\"Unemployed applicants do not meet employment eligibility requirements\");\n" +
                    "end\n"),

            rule("reject_high_dti", "Reject applications with debt-to-income ratio above 50%",
                    LoanRule.RuleCategory.DEBT_TO_INCOME, 90, true, pkg +
                    "rule \"Reject - High Debt to Income Ratio\"\n" +
                    "    salience 90\n" +
                    "    when\n" +
                    "        $fact : LoanApplicationFact( debtToIncomeRatio > 50.0 )\n" +
                    "        $result : DecisionResult( decision == null || decision.isEmpty() )\n" +
                    "    then\n" +
                    "        $result.setDecision(\"REJECTED\");\n" +
                    "        $result.setRiskTier(\"VERY_HIGH\");\n" +
                    "        $result.setRiskScore(88.0);\n" +
                    "        $result.addReason(\"Debt-to-income ratio of \" + $fact.getDebtToIncomeRatio() + \"% exceeds the maximum allowed threshold of 50%\");\n" +
                    "end\n"),

            rule("reject_excessive_loan", "Reject when loan amount exceeds 5x annual income",
                    LoanRule.RuleCategory.LOAN_AMOUNT, 85, true, pkg +
                    "rule \"Reject - Loan Exceeds Income Multiple\"\n" +
                    "    salience 85\n" +
                    "    when\n" +
                    "        $fact : LoanApplicationFact( loanToIncomeRatio > 5.0 )\n" +
                    "        $result : DecisionResult( decision == null || decision.isEmpty() )\n" +
                    "    then\n" +
                    "        $result.setDecision(\"REJECTED\");\n" +
                    "        $result.setRiskTier(\"HIGH\");\n" +
                    "        $result.setRiskScore(80.0);\n" +
                    "        $result.addReason(\"Requested loan amount exceeds 5x annual income. Loan-to-income ratio: \" + $fact.getLoanToIncomeRatio());\n" +
                    "end\n"),

            rule("manual_review_fair_credit", "Route to manual review for fair credit scores (580-649)",
                    LoanRule.RuleCategory.CREDIT_SCORE, 50, true, pkg +
                    "rule \"Manual Review - Fair Credit Score\"\n" +
                    "    salience 50\n" +
                    "    when\n" +
                    "        $fact : LoanApplicationFact( creditScore >= 580, creditScore < 650 )\n" +
                    "        $result : DecisionResult( decision == null || decision.isEmpty() )\n" +
                    "    then\n" +
                    "        $result.setDecision(\"MANUAL_REVIEW\");\n" +
                    "        $result.setRiskTier(\"HIGH\");\n" +
                    "        $result.setRiskScore(65.0);\n" +
                    "        $result.addReason(\"Credit score of \" + $fact.getCreditScore() + \" falls in the fair range (580-649) and requires analyst review\");\n" +
                    "end\n"),

            rule("manual_review_elevated_dti", "Route to manual review for elevated DTI (40-50%)",
                    LoanRule.RuleCategory.DEBT_TO_INCOME, 45, true, pkg +
                    "rule \"Manual Review - Elevated DTI\"\n" +
                    "    salience 45\n" +
                    "    when\n" +
                    "        $fact : LoanApplicationFact( debtToIncomeRatio >= 40.0, debtToIncomeRatio <= 50.0 )\n" +
                    "        $result : DecisionResult( decision == null || decision.isEmpty() )\n" +
                    "    then\n" +
                    "        $result.setDecision(\"MANUAL_REVIEW\");\n" +
                    "        $result.setRiskTier(\"MEDIUM\");\n" +
                    "        $result.setRiskScore(55.0);\n" +
                    "        $result.addReason(\"Debt-to-income ratio of \" + $fact.getDebtToIncomeRatio() + \"% is elevated. Manual review required\");\n" +
                    "end\n"),

            rule("approve_good_credit", "Auto-approve good credit profiles (650-749, DTI<=40%)",
                    LoanRule.RuleCategory.CREDIT_SCORE, 25, true, pkg +
                    "rule \"Approve - Good Credit Profile\"\n" +
                    "    salience 25\n" +
                    "    when\n" +
                    "        $fact : LoanApplicationFact( creditScore >= 650, creditScore < 750, debtToIncomeRatio <= 40.0,\n" +
                    "                                     employmentType != \"UNEMPLOYED\" )\n" +
                    "        $result : DecisionResult( decision == null || decision.isEmpty() )\n" +
                    "    then\n" +
                    "        $result.setDecision(\"APPROVED\");\n" +
                    "        $result.setApprovedAmount($fact.getRequestedAmount());\n" +
                    "        $result.setInterestRate(9.5);\n" +
                    "        $result.setApprovedTermMonths($fact.getTermMonths());\n" +
                    "        $result.setRiskTier(\"MEDIUM\");\n" +
                    "        $result.setRiskScore(35.0);\n" +
                    "        $result.addReason(\"Good credit profile approved. Credit score: \" + $fact.getCreditScore() + \", DTI: \" + $fact.getDebtToIncomeRatio() + \"%\");\n" +
                    "end\n"),

            rule("approve_excellent_credit", "Auto-approve excellent credit profiles (>=750, DTI<=30%)",
                    LoanRule.RuleCategory.CREDIT_SCORE, 30, true, pkg +
                    "rule \"Approve - Excellent Credit Profile\"\n" +
                    "    salience 30\n" +
                    "    when\n" +
                    "        $fact : LoanApplicationFact( creditScore >= 750, debtToIncomeRatio <= 30.0,\n" +
                    "                                     employmentType != \"UNEMPLOYED\" )\n" +
                    "        $result : DecisionResult( decision == null || decision.isEmpty() )\n" +
                    "    then\n" +
                    "        $result.setDecision(\"APPROVED\");\n" +
                    "        $result.setApprovedAmount($fact.getRequestedAmount());\n" +
                    "        $result.setInterestRate(6.5);\n" +
                    "        $result.setApprovedTermMonths($fact.getTermMonths());\n" +
                    "        $result.setRiskTier(\"LOW\");\n" +
                    "        $result.setRiskScore(15.0);\n" +
                    "        $result.addReason(\"Excellent credit profile approved. Credit score: \" + $fact.getCreditScore() + \", DTI: \" + $fact.getDebtToIncomeRatio() + \"%\");\n" +
                    "end\n"),

            rule("bonus_rate_long_employment", "Reduce interest rate by 0.5% for 5+ years at job",
                    LoanRule.RuleCategory.INTEREST_RATE, 10, true, pkg +
                    "rule \"Bonus Rate - Long Term Employment\"\n" +
                    "    salience 10\n" +
                    "    when\n" +
                    "        $fact : LoanApplicationFact( yearsAtCurrentJob >= 5, employmentType == \"EMPLOYED\" )\n" +
                    "        $result : DecisionResult( decision == \"APPROVED\" )\n" +
                    "    then\n" +
                    "        double newRate = $result.getInterestRate() - 0.5;\n" +
                    "        $result.setInterestRate(newRate < 3.0 ? 3.0 : newRate);\n" +
                    "        $result.addReason(\"Employment stability bonus: rate reduced by 0.5% for 5+ years at current employer\");\n" +
                    "end\n"),

            rule("self_employed_risk_adjustment", "Add 1% interest rate for self-employed applicants",
                    LoanRule.RuleCategory.INTEREST_RATE, 8, true, pkg +
                    "rule \"Risk Adjustment - Self Employed\"\n" +
                    "    salience 8\n" +
                    "    when\n" +
                    "        $fact : LoanApplicationFact( employmentType == \"SELF_EMPLOYED\" )\n" +
                    "        $result : DecisionResult( decision == \"APPROVED\" )\n" +
                    "    then\n" +
                    "        $result.setInterestRate($result.getInterestRate() + 1.0);\n" +
                    "        $result.addReason(\"Self-employment risk adjustment: +1.0% interest rate\");\n" +
                    "end\n"),

            rule("mortgage_ltv_check", "Route high LTV mortgages to manual review",
                    LoanRule.RuleCategory.LOAN_AMOUNT, 60, true, pkg +
                    "rule \"Manual Review - High LTV Mortgage\"\n" +
                    "    salience 60\n" +
                    "    when\n" +
                    "        $fact : LoanApplicationFact( loanType == \"MORTGAGE\", loanToValueRatio > 90.0 )\n" +
                    "        $result : DecisionResult( decision == null || decision.isEmpty() )\n" +
                    "    then\n" +
                    "        $result.setDecision(\"MANUAL_REVIEW\");\n" +
                    "        $result.setRiskTier(\"HIGH\");\n" +
                    "        $result.setRiskScore(70.0);\n" +
                    "        $result.addReason(\"Mortgage LTV ratio of \" + $fact.getLoanToValueRatio() + \"% exceeds 90%. Additional underwriting required\");\n" +
                    "end\n")
        );

        ruleRepository.saveAll(rules);
        log.info("Seeded {} loan decision rules", rules.size());
    }

    private LoanRule rule(String name, String desc, LoanRule.RuleCategory category,
                          int salience, boolean active, String drl) {
        LoanRule r = new LoanRule();
        r.setRuleName(name);
        r.setRuleDescription(desc);
        r.setCategory(category);
        r.setSalience(salience);
        r.setActive(active);
        r.setDrlContent(drl);
        r.setCreatedBy("system");
        r.setLastModifiedBy("system");
        return r;
    }
}
