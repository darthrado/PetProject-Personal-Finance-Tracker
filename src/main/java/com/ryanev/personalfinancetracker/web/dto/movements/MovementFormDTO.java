package com.ryanev.personalfinancetracker.web.dto.movements;

import com.ryanev.personalfinancetracker.data.entities.Movement;

import java.time.LocalDate;

public class MovementFormDTO {
    private Long id;
    private Double unsignedAmount;
    private LocalDate valueDate;
    private String name;
    private String categoryName;
    private Boolean flagAmountPositive;
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getUnsignedAmount() {
        return unsignedAmount;
    }

    public void setUnsignedAmount(Double unsignedAmount) {
        this.unsignedAmount = unsignedAmount;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }

    public void setValueDate(LocalDate valueDate) {
        this.valueDate = valueDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Boolean getFlagAmountPositive() {
        return flagAmountPositive;
    }

    public void setFlagAmountPositive(Boolean flagAmountPositive) {
        this.flagAmountPositive = flagAmountPositive;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
