package com.ryanev.personalfinancetracker.web.dto.movements;

import com.ryanev.personalfinancetracker.data.entities.Movement;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

public class MovementFormDTO {
    private Long id;

    @NotNull @Positive
    private Double unsignedAmount;

    @NotNull
    private LocalDate valueDate;

    @NotEmpty
    private String name;

    @NotNull
    private String categoryName;

    @NotNull
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
