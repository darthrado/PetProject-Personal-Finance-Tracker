package com.ryanev.personalfinancetracker.services;

import com.ryanev.personalfinancetracker.entities.Movement;
import com.ryanev.personalfinancetracker.exceptions.InvalidMovementException;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

public interface MovementsService {
    List<Movement> getAll();
    List<Movement> getMovementsForUser(Long userId); //TODO figure out how to pass optional search filter
    List<Movement> getMovementsForUserAndPeriod(Long userId, LocalDate startDate, LocalDate endDate);
    Movement saveMovement(Movement newMovement) throws InvalidMovementException;
    Movement getMovementById(Long movementId) throws NoSuchElementException;
    void deleteMovement(Movement movement);
    void deleteMovementById(Long id);
}
