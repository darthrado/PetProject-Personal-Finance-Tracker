package com.ryanev.personalfinancetracker.dao;

import com.ryanev.personalfinancetracker.entities.Movement;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MovementsRepository extends CrudRepository<Movement,Long> {
    @Override
    List<Movement> findAll();

    List<Movement> findAllByUserId(Long userId);
}
