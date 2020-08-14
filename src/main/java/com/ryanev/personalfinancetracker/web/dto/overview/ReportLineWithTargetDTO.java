package com.ryanev.personalfinancetracker.web.dto.overview;

public class ReportLineWithTargetDTO {

    private String categoryName;
    private Double amount;
    private Double target;

    public ReportLineWithTargetDTO() {
    }

    public ReportLineWithTargetDTO(String categoryName, Double amount, Double target) {
        this.categoryName = categoryName;
        this.amount = Math.abs(amount);
        this.target = target;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setTarget(Double target) {
        this.target = target;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Double getAmount() {
        return amount;
    }

    public Double getTarget() {
        return target;
    }
}
