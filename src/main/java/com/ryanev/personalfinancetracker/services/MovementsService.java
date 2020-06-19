package com.ryanev.personalfinancetracker.services;

import com.ryanev.personalfinancetracker.entities.Movement;

import java.util.List;

public interface MovementsService {
    List<Movement> getMovementsForUser(Long userId); //TODO figure out how to pass optional search filter
    Movement saveMovement(Movement newMovement);
    Movement getMovementById(Long movementId);
    void deleteMovement(Movement movement);
    void deleteMovementById(Long id);
}
