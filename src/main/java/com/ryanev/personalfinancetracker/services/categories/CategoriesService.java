package com.ryanev.personalfinancetracker.services.categories;

import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import com.ryanev.personalfinancetracker.exceptions.IncorrectCategoryIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.exceptions.InvalidCategoryException;
import com.ryanev.personalfinancetracker.services.dto.categories.CategoryDTO;

import java.util.List;
import java.util.NoSuchElementException;

public interface CategoriesService {
    List<CategoryDTO> getAll();
    List<CategoryDTO> getActiveCategoriesForUser(Long userId);
    CategoryDTO getCategoryById(Long categoryId) throws NoSuchElementException;
    CategoryDTO saveCategory(CategoryDTO category)  throws InvalidCategoryException;
    void changeCategoryFlagActive(Long categoryId,Boolean flagActive) throws IncorrectCategoryIdException;
    void deleteCategoryById(Long categoryId) throws IncorrectCategoryIdException;
    List<CategoryDTO> getDefaultCategoriesForUser(Long userId);
    void createDefaultCategoriesForUser(Long userId) throws IncorrectUserIdException;
    Boolean isCategoryDefault(String categoryName);
    Boolean existsById(Long id);

    List<CategoryDTO> getCategoriesForUser(Long userId);


}
