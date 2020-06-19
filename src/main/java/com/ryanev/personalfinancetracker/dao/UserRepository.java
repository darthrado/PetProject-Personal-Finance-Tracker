package com.ryanev.personalfinancetracker.dao;

import com.ryanev.personalfinancetracker.entities.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User,Long> {
}
