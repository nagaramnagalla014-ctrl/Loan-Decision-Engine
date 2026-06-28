package com.loan.decisionengine.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class RuleUpdateRequest {

    @NotBlank
    private String ruleName;
    @NotBlank
    private String ruleDescription;
    @NotBlank
    private String category;
    @NotBlank
    private String drlContent;
    @NotNull
    private Integer salience;
    private Boolean active;

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getRuleDescription() { return ruleDescription; }
    public void setRuleDescription(String ruleDescription) { this.ruleDescription = ruleDescription; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDrlContent() { return drlContent; }
    public void setDrlContent(String drlContent) { this.drlContent = drlContent; }
    public Integer getSalience() { return salience; }
    public void setSalience(Integer salience) { this.salience = salience; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
