package com.ryanev.personalfinancetracker.services;

import com.ryanev.personalfinancetracker.entities.User;

import java.util.List;

public interface UserService {
    List<User> getAll();
    User getUserById(long userId);
}
