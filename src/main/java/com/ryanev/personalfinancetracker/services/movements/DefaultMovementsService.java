package com.ryanev.personalfinancetracker.services.movements;
import com.ryanev.personalfinancetracker.data.repo.movements.MovementsRepository;
import com.ryanev.personalfinancetracker.data.entities.Movement;
import com.ryanev.personalfinancetracker.exceptions.InvalidMovementException;
import com.ryanev.personalfinancetracker.services.categories.CategoriesService;
import com.ryanev.personalfinancetracker.services.dto.movements.MovementDTO;
import com.ryanev.personalfinancetracker.services.movements.MovementsService;
import com.ryanev.personalfinancetracker.services.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class DefaultMovementsService implements MovementsService {
    @Autowired
    private UserService userService;
    @Autowired
    private MovementsRepository movementsRepository;
    @Autowired
    private CategoriesService categoriesService;

    private void validateMovement(Movement movementForValidation) throws InvalidMovementException {
        if (movementForValidation == null)
            throw new InvalidMovementException("Cannot save null movement");

        if (movementForValidation.getCategory() == null){
            throw new InvalidMovementException("Category cannot be null");
        }
        if (movementForValidation.getUser() == null){
            throw new InvalidMovementException("User cannot be null");
        }
        if (!userService.existsById(movementForValidation.getUser().getId())){
            throw new InvalidMovementException("User not found");
        }
        if (!categoriesService.existsById(movementForValidation.getCategory().getId())){
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

        Movement toReturn =  movementsRepository.save(newMovement);
        userService.updateCacheWithMovementDate(newMovement.getUser().getId(),newMovement.getValueDate());
        return toReturn;
    }

    @Override
    public Movement getMovementById(Long movementId) throws NoSuchElementException {
        return movementsRepository.findById(movementId).orElseThrow();
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

    @Override
    public List<MovementDTO> getMovementsForUserAndPeriod(Long userId, LocalDate startDate, LocalDate endDate) {
        return movementsRepository.findAllByUserIdAndPeriod(userId,startDate,endDate)
                .stream()
                .map(this::mapMovementToDTO)
                .collect(Collectors.toList());

    }

    private MovementDTO mapMovementToDTO(Movement movement){
        MovementDTO newDTO = new MovementDTO();
        newDTO.setId(movement.getId());
        newDTO.setAmount(movement.getAmount());
        newDTO.setDate(movement.getValueDate());
        newDTO.setName(movement.getName());
        newDTO.setCategory(movement.getCategory().getName());

        return newDTO;
    }

}
