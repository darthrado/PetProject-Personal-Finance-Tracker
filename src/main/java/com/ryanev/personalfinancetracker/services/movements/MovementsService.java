package com.ryanev.personalfinancetracker.services.movements;

import com.ryanev.personalfinancetracker.data.entities.Movement;
import com.ryanev.personalfinancetracker.exceptions.InvalidMovementException;
import com.ryanev.personalfinancetracker.services.dto.movements.MovementDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

public interface MovementsService {
    List<MovementDTO> getMovementsForUser(Long userId); //TODO figure out how to pass optional search filter
    List<MovementDTO> getMovementsForUserAndPeriod(Long userId, LocalDate startDate, LocalDate endDate);
    MovementDTO saveMovement(MovementDTO newMovement) throws InvalidMovementException;
    MovementDTO getMovementById(Long movementId) throws NoSuchElementException;
    void deleteMovementById(Long id);
}
