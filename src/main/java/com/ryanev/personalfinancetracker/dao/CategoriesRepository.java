package com.ryanev.personalfinancetracker.dao;

import com.ryanev.personalfinancetracker.entities.MovementCategory;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CategoriesRepository extends CrudRepository<MovementCategory,Long> {

    List<MovementCategory> findAllByUserId(Long userId);

    @Override
    List<MovementCategory> findAll();
}
