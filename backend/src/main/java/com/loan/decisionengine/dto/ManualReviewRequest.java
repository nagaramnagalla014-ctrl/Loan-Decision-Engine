package com.loan.decisionengine.dto;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class ManualReviewRequest {

    @NotBlank
    private String decision;
    private String reviewNotes;
    private BigDecimal approvedAmount;
    private BigDecimal interestRate;
    private Integer approvedTermMonths;

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }
    public BigDecimal getApprovedAmount() { return approvedAmount; }
    public void setApprovedAmount(BigDecimal approvedAmount) { this.approvedAmount = approvedAmount; }
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    public Integer getApprovedTermMonths() { return approvedTermMonths; }
    public void setApprovedTermMonths(Integer approvedTermMonths) { this.approvedTermMonths = approvedTermMonths; }
}
