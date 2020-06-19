package com.ryanev.personalfinancetracker.services;

import com.ryanev.personalfinancetracker.entities.MovementCategory;

import java.util.List;

public interface CategoriesService {
    List<MovementCategory> getCategoriesForUser(Long userId);
    MovementCategory getCategoryById(Long categoryId);
}
