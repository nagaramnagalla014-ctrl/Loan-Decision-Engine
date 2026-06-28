package com.loan.decisionengine.kafka;

import java.time.LocalDateTime;

public class RuleUpdateEvent {

    private String action;
    private Long ruleId;
    private String ruleName;
    private String updatedBy;
    private String engineVersion;
    private LocalDateTime eventTime;

    public RuleUpdateEvent() {}

    public RuleUpdateEvent(String action, Long ruleId, String ruleName,
                            String updatedBy, String engineVersion) {
        this.action = action;
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.updatedBy = updatedBy;
        this.engineVersion = engineVersion;
        this.eventTime = LocalDateTime.now();
    }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public String getEngineVersion() { return engineVersion; }
    public void setEngineVersion(String engineVersion) { this.engineVersion = engineVersion; }
    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
}
