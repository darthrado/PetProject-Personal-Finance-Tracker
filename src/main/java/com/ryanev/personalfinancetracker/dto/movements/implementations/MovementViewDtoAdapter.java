package com.ryanev.personalfinancetracker.dto.movements.implementations;

import com.ryanev.personalfinancetracker.dto.movements.MovementViewDTO;
import com.ryanev.personalfinancetracker.data.entities.Movement;

import java.time.LocalDate;

public class MovementViewDtoAdapter implements MovementViewDTO {

    private final Movement sourceMovement;

    public MovementViewDtoAdapter(Movement sourceMovement) {
        this.sourceMovement = sourceMovement;
    }

    @Override
    public Long getId() {
        return sourceMovement.getId();
    }

    @Override
    public LocalDate getValueDate() {
        return sourceMovement.getValueDate();
    }

    @Override
    public Double getSignedAmount() {
        return sourceMovement.getAmount();
    }

    @Override
    public String getName() {
        return sourceMovement.getName();
    }

    @Override
    public String getCategoryName() {
        return sourceMovement.getCategory().getName();
    }
}
