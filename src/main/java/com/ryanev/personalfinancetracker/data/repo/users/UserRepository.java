package com.ryanev.personalfinancetracker.data.repo.users;

import com.ryanev.personalfinancetracker.data.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User,Long> {
    @Override
    List<User> findAll();

    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
}
