package com.ryanev.personalfinancetracker.services.dto.targets;


public class TargetExpensesAndAmountDTO {

    private Long targetId;
    private String categoryName;
    private Double amount;

    public TargetExpensesAndAmountDTO(){

    }

    public TargetExpensesAndAmountDTO(Long targetId,Double amount) {
        this.targetId = targetId;
        this.amount = amount;
    }

    public TargetExpensesAndAmountDTO(Long targetId, String categoryName, Double amount) {
        this.targetId = targetId;
        this.categoryName = categoryName;
        this.amount = amount;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
