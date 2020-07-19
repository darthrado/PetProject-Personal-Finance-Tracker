package com.ryanev.personalfinancetracker.dao;

import com.ryanev.personalfinancetracker.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User,Long> {
    @Override
    List<User> findAll();
}
