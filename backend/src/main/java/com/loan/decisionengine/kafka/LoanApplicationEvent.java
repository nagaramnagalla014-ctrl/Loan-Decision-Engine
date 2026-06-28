package com.loan.decisionengine.kafka;

import java.time.LocalDateTime;

public class LoanApplicationEvent {

    private String eventType;
    private Long applicationId;
    private String applicationNumber;
    private String applicantEmail;
    private String loanType;
    private double requestedAmount;
    private String status;
    private String decision;
    private String ruleEngineVersion;
    private LocalDateTime eventTime;

    public LoanApplicationEvent() {}

    public static LoanApplicationEvent submitted(Long applicationId, String applicationNumber,
                                                  String applicantEmail, String loanType,
                                                  double requestedAmount) {
        LoanApplicationEvent event = new LoanApplicationEvent();
        event.eventType = "APPLICATION_SUBMITTED";
        event.applicationId = applicationId;
        event.applicationNumber = applicationNumber;
        event.applicantEmail = applicantEmail;
        event.loanType = loanType;
        event.requestedAmount = requestedAmount;
        event.status = "SUBMITTED";
        event.eventTime = LocalDateTime.now();
        return event;
    }

    public static LoanApplicationEvent decided(Long applicationId, String applicationNumber,
                                                String applicantEmail, String decision,
                                                String engineVersion) {
        LoanApplicationEvent event = new LoanApplicationEvent();
        event.eventType = "DECISION_COMPLETED";
        event.applicationId = applicationId;
        event.applicationNumber = applicationNumber;
        event.applicantEmail = applicantEmail;
        event.decision = decision;
        event.status = decision;
        event.ruleEngineVersion = engineVersion;
        event.eventTime = LocalDateTime.now();
        return event;
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public String getApplicationNumber() { return applicationNumber; }
    public void setApplicationNumber(String applicationNumber) { this.applicationNumber = applicationNumber; }
    public String getApplicantEmail() { return applicantEmail; }
    public void setApplicantEmail(String applicantEmail) { this.applicantEmail = applicantEmail; }
    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }
    public double getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(double requestedAmount) { this.requestedAmount = requestedAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getRuleEngineVersion() { return ruleEngineVersion; }
    public void setRuleEngineVersion(String ruleEngineVersion) { this.ruleEngineVersion = ruleEngineVersion; }
    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
}
