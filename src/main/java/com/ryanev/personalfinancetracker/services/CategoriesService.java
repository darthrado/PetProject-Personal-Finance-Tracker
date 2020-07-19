package com.ryanev.personalfinancetracker.services;

import com.ryanev.personalfinancetracker.entities.MovementCategory;
import com.ryanev.personalfinancetracker.exceptions.InvalidCategoryException;

import java.util.List;
import java.util.NoSuchElementException;

public interface CategoriesService {
    List<MovementCategory> getAll();
    List<MovementCategory> getCategoriesForUser(Long userId);
    MovementCategory getCategoryById(Long categoryId) throws NoSuchElementException;
    MovementCategory saveCategory(MovementCategory category)  throws InvalidCategoryException;
    void deleteCategoryById(Long categoryId);

}
