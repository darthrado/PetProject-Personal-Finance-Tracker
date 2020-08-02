package com.ryanev.personalfinancetracker.dto.movements.implementations;

import com.ryanev.personalfinancetracker.dto.movements.MovementFormDTO;
import com.ryanev.personalfinancetracker.entities.Movement;

import java.time.LocalDate;

public class DefaultMovementFormDTO implements MovementFormDTO {
    private Long id;
    private Double unsignedAmount;
    private LocalDate valueDate;
    private String name;
    private Long categoryId;
    private Boolean flagAmountPositive;
    private String description;

    public DefaultMovementFormDTO() {
    }
    public DefaultMovementFormDTO(Movement movement){
        id = movement.getId();
        if (movement.getAmount() != null){
            unsignedAmount = Math.abs(movement.getAmount());
            flagAmountPositive = (movement.getAmount() > 0);
        }
        valueDate = movement.getValueDate();
        name = movement.getName();
        if (movement.getCategory() != null){
            categoryId = movement.getCategory().getId();
        }
        description = movement.getDescription();

    }

    @Override
    public Long getCategoryId() {
        return categoryId;
    }

    @Override
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public LocalDate getValueDate() {
        return valueDate;
    }

    @Override
    public void setValueDate(LocalDate valueDate) {
        this.valueDate = valueDate;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Double getUnsignedAmount() {
        return unsignedAmount;
    }

    @Override
    public void setUnsignedAmount(Double amount) {
        unsignedAmount = amount;
    }

    @Override
    public Boolean getFlagAmountPositive() {
        return flagAmountPositive;
    }

    @Override
    public void setFlagAmountPositive(Boolean flag) {
        flagAmountPositive = flag;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }
}
