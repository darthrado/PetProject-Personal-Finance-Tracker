package com.ryanev.personalfinancetracker.services.implementation;

import com.ryanev.personalfinancetracker.dao.CategoriesRepository;
import com.ryanev.personalfinancetracker.dao.UserRepository;
import com.ryanev.personalfinancetracker.entities.MovementCategory;
import com.ryanev.personalfinancetracker.exceptions.InvalidCategoryException;
import com.ryanev.personalfinancetracker.exceptions.InvalidMovementException;
import com.ryanev.personalfinancetracker.services.CategoriesService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DefaultCategoriesService implements CategoriesService {
    @Autowired
    private CategoriesRepository categoriesRepository;
    @Autowired
    private UserRepository userRepository;

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

        validateCategory(category);

        try {
            return categoriesRepository.save(category);
        }
        catch (DataIntegrityViolationException e){
            //TODO handle the violated constraint specifically; rethrow the exception otherwise
            throw new InvalidCategoryException("Save would violate data integrity");
        }
    }

    @Override
    public void deleteCategoryById(Long categoryId) {

    }

    @Override
    public List<MovementCategory> getAll() {
        return categoriesRepository.findAll();
    }
}
