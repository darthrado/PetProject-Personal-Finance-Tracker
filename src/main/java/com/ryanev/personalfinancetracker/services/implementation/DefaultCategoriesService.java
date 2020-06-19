package com.ryanev.personalfinancetracker.services.implementation;

import com.ryanev.personalfinancetracker.dao.CategoriesRepository;
import com.ryanev.personalfinancetracker.entities.MovementCategory;
import com.ryanev.personalfinancetracker.services.CategoriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultCategoriesService implements CategoriesService {
    @Autowired
    private CategoriesRepository categoriesRepository;

    @Override
    public List<MovementCategory> getCategoriesForUser(Long userId) {
        return categoriesRepository.findAllByUserId(userId);
    }

    @Override
    public MovementCategory getCategoryById(Long categoryId) {
        return categoriesRepository.findById(categoryId).orElseThrow();
    }
}
