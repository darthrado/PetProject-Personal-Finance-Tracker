package com.ryanev.personalfinancetracker.data.repo.categories;

import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriesRepository extends CrudRepository<MovementCategory,Long> {

    List<MovementCategory> findAllByUserId(Long userId);

    @Override
    List<MovementCategory> findAll();

    Optional<MovementCategory> findByUserIdAndName(Long userId, String name);
}
