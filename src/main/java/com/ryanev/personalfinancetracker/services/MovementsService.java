package com.ryanev.personalfinancetracker.services;

import com.ryanev.personalfinancetracker.entities.Movement;
import com.ryanev.personalfinancetracker.exceptions.IncorrectMovementIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.exceptions.InvalidMovementException;
import org.thymeleaf.spring5.expression.Mvc;

import java.util.List;
import java.util.NoSuchElementException;

public interface MovementsService {
    List<Movement> getAll();
    List<Movement> getMovementsForUser(Long userId); //TODO figure out how to pass optional search filter
    Movement saveMovement(Movement newMovement) throws InvalidMovementException;
    Movement getMovementById(Long movementId) throws NoSuchElementException;
    void deleteMovement(Movement movement);
    void deleteMovementById(Long id);
}
