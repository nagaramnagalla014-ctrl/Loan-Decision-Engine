package com.loan.decisionengine.dto;

import javax.validation.constraints.*;
import java.math.BigDecimal;

public class LoanApplicationRequest {

    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @Email @NotBlank
    private String email;
    private String phone;

    @NotBlank
    private String loanType;

    @NotNull @DecimalMin("1000.00")
    private BigDecimal requestedAmount;

    @NotNull @Min(6) @Max(360)
    private Integer termMonths;

    @NotNull @DecimalMin("1.00")
    private BigDecimal annualIncome;

    @NotNull @DecimalMin("0.00")
    private BigDecimal monthlyDebtPayments;

    @NotNull @Min(300) @Max(850)
    private Integer creditScore;

    @NotBlank
    private String employmentType;

    @Min(0)
    private Integer yearsAtCurrentJob;

    @DecimalMin("0.00")
    private BigDecimal collateralValue;

    @Size(max = 1000)
    private String loanPurpose;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }
    public Integer getTermMonths() { return termMonths; }
    public void setTermMonths(Integer termMonths) { this.termMonths = termMonths; }
    public BigDecimal getAnnualIncome() { return annualIncome; }
    public void setAnnualIncome(BigDecimal annualIncome) { this.annualIncome = annualIncome; }
    public BigDecimal getMonthlyDebtPayments() { return monthlyDebtPayments; }
    public void setMonthlyDebtPayments(BigDecimal monthlyDebtPayments) { this.monthlyDebtPayments = monthlyDebtPayments; }
    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }
    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
    public Integer getYearsAtCurrentJob() { return yearsAtCurrentJob; }
    public void setYearsAtCurrentJob(Integer yearsAtCurrentJob) { this.yearsAtCurrentJob = yearsAtCurrentJob; }
    public BigDecimal getCollateralValue() { return collateralValue; }
    public void setCollateralValue(BigDecimal collateralValue) { this.collateralValue = collateralValue; }
    public String getLoanPurpose() { return loanPurpose; }
    public void setLoanPurpose(String loanPurpose) { this.loanPurpose = loanPurpose; }
}
