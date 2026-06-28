package com.loan.decisionengine.drools.facts;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LoanApplicationFact {

    private Long applicationId;
    private String loanType;
    private double requestedAmount;
    private int termMonths;
    private double annualIncome;
    private double monthlyDebtPayments;
    private int creditScore;
    private String employmentType;
    private int yearsAtCurrentJob;
    private double collateralValue;

    // Computed ratios
    private double debtToIncomeRatio;
    private double loanToIncomeRatio;
    private double loanToValueRatio;

    public LoanApplicationFact() {}

    public LoanApplicationFact(Long applicationId, String loanType, double requestedAmount,
                                int termMonths, double annualIncome, double monthlyDebtPayments,
                                int creditScore, String employmentType, int yearsAtCurrentJob,
                                double collateralValue) {
        this.applicationId = applicationId;
        this.loanType = loanType;
        this.requestedAmount = requestedAmount;
        this.termMonths = termMonths;
        this.annualIncome = annualIncome;
        this.monthlyDebtPayments = monthlyDebtPayments;
        this.creditScore = creditScore;
        this.employmentType = employmentType;
        this.yearsAtCurrentJob = yearsAtCurrentJob;
        this.collateralValue = collateralValue;
        computeRatios();
    }

    private void computeRatios() {
        double monthlyIncome = annualIncome / 12.0;
        this.debtToIncomeRatio = (monthlyIncome > 0)
                ? round((monthlyDebtPayments / monthlyIncome) * 100.0) : 100.0;
        this.loanToIncomeRatio = (annualIncome > 0)
                ? round(requestedAmount / annualIncome) : 999.0;
        this.loanToValueRatio = (collateralValue > 0)
                ? round((requestedAmount / collateralValue) * 100.0) : 0.0;
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public Long getApplicationId() { return applicationId; }
    public String getLoanType() { return loanType; }
    public double getRequestedAmount() { return requestedAmount; }
    public int getTermMonths() { return termMonths; }
    public double getAnnualIncome() { return annualIncome; }
    public double getMonthlyDebtPayments() { return monthlyDebtPayments; }
    public int getCreditScore() { return creditScore; }
    public String getEmploymentType() { return employmentType; }
    public int getYearsAtCurrentJob() { return yearsAtCurrentJob; }
    public double getCollateralValue() { return collateralValue; }
    public double getDebtToIncomeRatio() { return debtToIncomeRatio; }
    public double getLoanToIncomeRatio() { return loanToIncomeRatio; }
    public double getLoanToValueRatio() { return loanToValueRatio; }
}
