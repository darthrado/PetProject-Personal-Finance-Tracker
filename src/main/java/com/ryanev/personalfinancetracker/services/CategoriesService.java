package com.ryanev.personalfinancetracker.services;

import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import com.ryanev.personalfinancetracker.exceptions.IncorrectCategoryIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.exceptions.InvalidCategoryException;

import java.util.List;
import java.util.NoSuchElementException;

public interface CategoriesService {
    List<MovementCategory> getAll();
    List<MovementCategory> getCategoriesForUser(Long userId);
    List<MovementCategory> getActiveCategoriesForUser(Long userId);
    MovementCategory getCategoryById(Long categoryId) throws NoSuchElementException;
    MovementCategory saveCategory(MovementCategory category)  throws InvalidCategoryException;
    void changeCategoryFlagActive(Long categoryId,Boolean flagActive) throws IncorrectCategoryIdException;
    void deleteCategoryById(Long categoryId) throws IncorrectCategoryIdException;
    List<MovementCategory> getDefaultCategoriesForUser(Long userId);
    void createDefaultCategoriesForUser(Long userId) throws IncorrectUserIdException;
    Boolean isCategoryDefault(String categoryName);
    Boolean existsById(Long id);
}
