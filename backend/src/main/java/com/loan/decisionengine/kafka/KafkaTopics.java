package com.loan.decisionengine.kafka;

public final class KafkaTopics {
    public static final String LOAN_APPLICATION_SUBMITTED = "loan-application-submitted";
    public static final String LOAN_DECISION_COMPLETED    = "loan-decision-completed";
    public static final String RULE_UPDATE_NOTIFICATION   = "rule-update-notification";
    public static final String LOAN_MANUAL_REVIEW         = "loan-manual-review";

    private KafkaTopics() {}
}
