package com.ryanev.personalfinancetracker.services.implementation;
import com.ryanev.personalfinancetracker.dao.CategoriesRepository;
import com.ryanev.personalfinancetracker.dao.MovementsRepository;
import com.ryanev.personalfinancetracker.dao.UserRepository;
import com.ryanev.personalfinancetracker.entities.Movement;
import com.ryanev.personalfinancetracker.exceptions.InvalidMovementException;
import com.ryanev.personalfinancetracker.services.MovementsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DefaultMovementsService implements MovementsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MovementsRepository movementsRepository;
    @Autowired
    private CategoriesRepository categoriesRepository;

    private void validateMovement(Movement movementForValidation) throws InvalidMovementException {
        if (movementForValidation == null)
            throw new InvalidMovementException("Cannot save null movement");

        if (movementForValidation.getCategory() == null){
            throw new InvalidMovementException("Category cannot be null");
        }
        if (movementForValidation.getUser() == null){
            throw new InvalidMovementException("User cannot be null");
        }
        if (!userRepository.existsById(movementForValidation.getUser().getId())){
            throw new InvalidMovementException("User not found");
        }
        if (!categoriesRepository.existsById(movementForValidation.getCategory().getId())){
            throw new InvalidMovementException("Category not found");
        }
        if (movementForValidation.getName() == null || movementForValidation.getName().isBlank()){
            throw new InvalidMovementException("Movement Name cannot be blank");
        }
        if (movementForValidation.getValueDate() == null){
            throw new InvalidMovementException("Movement Value Date cannot be empty");
        }
        if (movementForValidation.getAmount() == 0){
            throw new InvalidMovementException("Movement amount cannot be 0");
        }
    }

    @Override
    public List<Movement> getMovementsForUser(Long userId) {
        return movementsRepository.findAllByUserId(userId);
    }
    @Override
    public Movement saveMovement(Movement newMovement) throws InvalidMovementException {

        validateMovement(newMovement);

        return movementsRepository.save(newMovement);
    }

    @Override
    public Movement getMovementById(Long movementId) throws NoSuchElementException {
        return movementsRepository.findById(movementId).orElseThrow();
    }

    @Override
    public void deleteMovement(Movement movement) {
        movementsRepository.delete(movement);
    }
    @Override
    public void deleteMovementById(Long id) {

        if (movementsRepository.existsById(id)){
            movementsRepository.deleteById(id);
        }
    }

    @Override
    public List<Movement> getAll() {
        return movementsRepository.findAll();
    }
}
