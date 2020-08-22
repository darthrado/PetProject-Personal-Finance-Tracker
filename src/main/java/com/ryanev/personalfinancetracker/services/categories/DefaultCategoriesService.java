package com.ryanev.personalfinancetracker.services.categories;

import com.ryanev.personalfinancetracker.data.repo.categories.CategoriesRepository;
import com.ryanev.personalfinancetracker.data.repo.movements.MovementsRepository;
import com.ryanev.personalfinancetracker.data.repo.users.UserRepository;
import com.ryanev.personalfinancetracker.data.entities.Movement;
import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import com.ryanev.personalfinancetracker.exceptions.IncorrectCategoryIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.exceptions.InvalidCategoryException;
import com.ryanev.personalfinancetracker.services.crud_observer.CrudChangeNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class DefaultCategoriesService implements CategoriesService {
    @Autowired
    private CategoriesRepository categoriesRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MovementsRepository movementsRepository;
    @Autowired
    private CategoryChangeNotifier categoryChangeNotifier;

    private final List<String> defaultCategoryNames = List.of("Income(DEFAULT)","Other Expenses(DEFAULT)");

    private void validateCategory(MovementCategory categoryForValidation) throws InvalidCategoryException {

        if(categoryForValidation == null){
            throw new InvalidCategoryException("Category cannot be null");
        }
        if (categoryForValidation.getUser() == null){
            throw new InvalidCategoryException("User cannot be null");
        }
        if (!userRepository.existsById(categoryForValidation.getUser().getId())){
            throw new InvalidCategoryException("User not found");
        }
        if(categoryForValidation.getName() == null || categoryForValidation.getName().isBlank()){
            throw new InvalidCategoryException("Name cannot be blank");
        }
    }

    @Override
    public List<MovementCategory> getCategoriesForUser(Long userId) {
        return categoriesRepository.findAllByUserId(userId);
    }

    @Override
    public MovementCategory getCategoryById(Long categoryId) throws NoSuchElementException {
        return categoriesRepository.findById(categoryId).orElseThrow();
    }

    @Override
    public MovementCategory saveCategory(MovementCategory category) throws InvalidCategoryException {

        Boolean flagCategoryExists=false;
        if(category!=null){
            flagCategoryExists = category.getId()==null?false:true;
        }


        MovementCategory savedCategory;
        validateCategory(category);

        try {
            savedCategory = categoriesRepository.save(category);
        }
        catch (DataIntegrityViolationException e){
            //TODO handle the violated constraint specifically; rethrow the exception otherwise
            throw new InvalidCategoryException("Save would violate data integrity");
        }

        if(!flagCategoryExists){
            categoryChangeNotifier.notifyAllObservers(savedCategory, CrudChangeNotifier.NewState.CREATE);
        }
        else {
            categoryChangeNotifier.notifyAllObservers(savedCategory, CrudChangeNotifier.NewState.UPDATE);
        }

        return savedCategory;
    }

    @Override
    @Transactional
    public void deleteCategoryById(Long categoryId) throws IncorrectCategoryIdException {

        MovementCategory categoryToDelete;

        try {
            categoryToDelete = getCategoryById(categoryId);
        }catch (NoSuchElementException e){
            throw new IncorrectCategoryIdException();
        }


        List<Movement> movementsToUpdate = movementsRepository.findAllByCategoryId(categoryId);
        MovementCategory newCategoryForMovements = getCategoryById(categoryToDelete.getFallbackCategoryId());

        movementsToUpdate.forEach(movement -> movement.setCategory(newCategoryForMovements));
        movementsRepository.saveAll(movementsToUpdate);

        categoryChangeNotifier.notifyAllObservers(categoryToDelete, CrudChangeNotifier.NewState.DELETE);

        categoriesRepository.deleteById(categoryId);
    }

    @Override
    public List<MovementCategory> getAll() {
        return categoriesRepository.findAll();
    }

    @Override
    public void changeCategoryFlagActive(Long categoryId,Boolean flagActive) throws IncorrectCategoryIdException {

        MovementCategory categoryToModify;

        try {
            categoryToModify = getCategoryById(categoryId);
        }catch (NoSuchElementException e){
            throw new IncorrectCategoryIdException();
        }

        categoryToModify.setFlagActive(flagActive);
        categoriesRepository.save(categoryToModify);
        categoryChangeNotifier.notifyAllObservers(categoryToModify, CrudChangeNotifier.NewState.UPDATE);
    }

    @Override
    public List<MovementCategory> getActiveCategoriesForUser(Long userId) {
        return categoriesRepository.findAllByUserId(userId).stream().filter(MovementCategory::getFlagActive).collect(Collectors.toList());
    }

    @Override
    public List<MovementCategory> getDefaultCategoriesForUser(Long userId) {
        return categoriesRepository.findAllByUserId(userId)
                .stream()
                .filter(category -> defaultCategoryNames.contains(category.getName()))
                .collect(Collectors.toList());
    }

    private MovementCategory createDefaultCategoryWithName(Long userId, String name) throws NoSuchElementException{
        MovementCategory toInsert = new MovementCategory();
        toInsert.setName(name);
        toInsert.setUser(userRepository.findById(userId).orElseThrow());
        toInsert.setFlagActive(true);

        return toInsert;
    }

    @Override
    public void createDefaultCategoriesForUser(Long userId) throws IncorrectUserIdException {
        if(!userRepository.existsById(userId))
            throw new IncorrectUserIdException();

        List<String> presentDefaultCategories = getDefaultCategoriesForUser(userId)
                .stream()
                .map(MovementCategory::getName)
                .collect(Collectors.toList());

        List<MovementCategory> toInsert;
        try {
            toInsert = defaultCategoryNames.stream()
                    .filter(name -> !presentDefaultCategories.contains(name))
                    .map(name -> createDefaultCategoryWithName(userId,name))
                    .collect(Collectors.toList());
        }catch (NoSuchElementException e){
            throw new IncorrectUserIdException();
        }

        categoriesRepository.saveAll(toInsert);
        categoryChangeNotifier.notifyAllObservers(toInsert, CrudChangeNotifier.NewState.CREATE);
    }

    @Override
    public Boolean isCategoryDefault(String categoryName) {
        return defaultCategoryNames.contains(categoryName);
    }

    @Override
    public Boolean existsById(Long id) {
        return categoriesRepository.existsById(id);
    }
}