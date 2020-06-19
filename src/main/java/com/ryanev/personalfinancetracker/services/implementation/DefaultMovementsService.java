package com.ryanev.personalfinancetracker.services.implementation;
import com.ryanev.personalfinancetracker.dao.MovementsRepository;
import com.ryanev.personalfinancetracker.entities.Movement;
import com.ryanev.personalfinancetracker.services.MovementsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultMovementsService implements MovementsService {
    @Autowired
    private MovementsRepository movementsRepository;

    @Override
    public List<Movement> getMovementsForUser(Long userId) {
        return movementsRepository.findAllByUserId(userId);
    }
    @Override
    public Movement saveMovement(Movement newMovement) {
        return movementsRepository.save(newMovement);
    }

    @Override
    public Movement getMovementById(Long movementId) {
        return movementsRepository.findById(movementId).orElseThrow();
    }

    @Override
    public void deleteMovement(Movement movement) {
        movementsRepository.delete(movement);
    }
    @Override
    public void deleteMovementById(Long id) {
        movementsRepository.deleteById(id);
    }
}
