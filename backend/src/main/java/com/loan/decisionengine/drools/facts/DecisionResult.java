package com.loan.decisionengine.drools.facts;

import java.util.ArrayList;
import java.util.List;

public class DecisionResult {

    private String decision = "";
    private double approvedAmount;
    private double interestRate;
    private int approvedTermMonths;
    private double riskScore;
    private String riskTier = "";
    private List<String> reasons = new ArrayList<>();

    public void addReason(String reason) {
        this.reasons.add(reason);
    }

    public boolean isDecided() {
        return decision != null && !decision.isEmpty();
    }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public double getApprovedAmount() { return approvedAmount; }
    public void setApprovedAmount(double approvedAmount) { this.approvedAmount = approvedAmount; }
    public double getInterestRate() { return interestRate; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }
    public int getApprovedTermMonths() { return approvedTermMonths; }
    public void setApprovedTermMonths(int approvedTermMonths) { this.approvedTermMonths = approvedTermMonths; }
    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) { this.riskScore = riskScore; }
    public String getRiskTier() { return riskTier; }
    public void setRiskTier(String riskTier) { this.riskTier = riskTier; }
    public List<String> getReasons() { return reasons; }
    public void setReasons(List<String> reasons) { this.reasons = reasons; }
}
