package com.ryanev.personalfinancetracker.services.movements;
import com.ryanev.personalfinancetracker.data.repo.categories.CategoriesRepository;
import com.ryanev.personalfinancetracker.data.repo.movements.MovementFilterConditions;
import com.ryanev.personalfinancetracker.data.repo.movements.MovementsRepository;
import com.ryanev.personalfinancetracker.data.entities.Movement;
import com.ryanev.personalfinancetracker.data.repo.users.UserRepository;
import com.ryanev.personalfinancetracker.exceptions.InvalidMovementException;
import com.ryanev.personalfinancetracker.services.crud_observer.CrudChangeNotifier;
import com.ryanev.personalfinancetracker.services.dto.movements.MovementDTO;
import com.ryanev.personalfinancetracker.services.dto.movements.MovementSearchFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class MovementsServiceImpl implements MovementsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MovementsRepository movementsRepository;
    @Autowired
    private CategoriesRepository categoriesRepository;
    @Autowired
    private MovementChangeNotifier movementChangeNotifier;

    private void validateMovement(MovementDTO movementForValidation) throws InvalidMovementException {
        if (movementForValidation == null)
            throw new InvalidMovementException("Cannot save null movement");

        if (movementForValidation.getCategory() == null){
            throw new InvalidMovementException("Category cannot be null");
        }
        if (!userRepository.existsById(movementForValidation.getUserId())){
            throw new InvalidMovementException("User not found");
        }
        if (!categoriesRepository.findByUserIdAndName(movementForValidation.getUserId(),movementForValidation.getCategory()).isPresent()){
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
    public List<MovementDTO> getMovementsForUser(Long userId) {

        return movementsRepository
                .findAll(MovementFilterConditions.withUserId(userId))
                .stream()
                .map(this::mapMovementToDTO)
                .collect(Collectors.toList());
    }


    @Override
    public MovementDTO saveMovement(MovementDTO movement) throws InvalidMovementException {

        validateMovement(movement);

        if(movement.getId()!=null){
            movementsRepository.findById(movement.getId());
        }

        Movement toSave = mapDtoToMovement(movement);
        toSave = movementsRepository.save(toSave);


        if(movement.getId()==null){
            movementChangeNotifier.notifyAllObservers(toSave, CrudChangeNotifier.NewState.CREATE);
            movement.setId(toSave.getId());
        }
        else {
            movementChangeNotifier.notifyAllObservers(toSave, CrudChangeNotifier.NewState.UPDATE);
        }

        return movement;
    }

    @Override
    public MovementDTO getMovementById(Long movementId) throws NoSuchElementException {
        Movement movement = movementsRepository.findById(movementId).orElseThrow();
        return mapMovementToDTO(movement);
    }

    @Override
    public List<MovementDTO> getMovementsFromFilter(MovementSearchFilter filter) {
        return movementsRepository.findAll(mapFilterToSpec(filter))
                .stream()
                .map(this::mapMovementToDTO)
                .collect(Collectors.toList());
    }

    private Specification<Movement> mapFilterToSpec(MovementSearchFilter filter){

        if(filter==null){
            return null;
        }

        Specification<Movement> result = MovementFilterConditions.withUserId(filter.getUserId())
                .and(MovementFilterConditions.withUserId(filter.getUserId()))
                .and(MovementFilterConditions.withUserName(filter.getUserName()))
                .and(MovementFilterConditions.withAmountFrom(filter.getAmountFrom()))
                .and(MovementFilterConditions.withAmountTo(filter.getAmountTo()))
                .and(MovementFilterConditions.withDateFrom(filter.getDateFrom()))
                .and(MovementFilterConditions.withDateTo(filter.getDateTo()))
                .and(MovementFilterConditions.withCategoryName(filter.getCategoryName()))
                .and(MovementFilterConditions.withName(filter.getName()));
        return result;
    }


    @Override
    public void deleteMovementById(Long id) {

        Optional<Movement> toDelete = movementsRepository.findById(id);

        if(!toDelete.equals(Optional.empty())){
            movementChangeNotifier.notifyAllObservers(toDelete.get(), CrudChangeNotifier.NewState.DELETE);
            movementsRepository.delete(toDelete.get());
        }
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
        newDTO.setValueDate(movement.getValueDate());
        newDTO.setName(movement.getName());
        newDTO.setCategory(movement.getCategory().getName());
        newDTO.setUserId(movement.getUser().getId());
        newDTO.setDescription(movement.getDescription());

        return newDTO;
    }

    private Movement mapDtoToMovement(MovementDTO dto){
        Movement movementEntity;

        if(dto.getId()!=null){
            movementEntity = movementsRepository.findById(dto.getId()).orElseThrow();
        }
        else {
            movementEntity = new Movement();
        }

        movementEntity.setName(dto.getName());
        movementEntity.setDescription(dto.getDescription());
        movementEntity.setAmount(dto.getAmount());
        movementEntity.setValueDate(dto.getValueDate());
        movementEntity.setUser(userRepository.findById(dto.getUserId()).orElseThrow());
        movementEntity.setCategory(categoriesRepository.findByUserIdAndName(dto.getUserId(),dto.getCategory()).orElseThrow());

        return movementEntity;
    }

}
