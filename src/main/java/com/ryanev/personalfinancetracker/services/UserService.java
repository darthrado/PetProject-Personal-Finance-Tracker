package com.ryanev.personalfinancetracker.services;

import com.ryanev.personalfinancetracker.entities.User;

public interface UserService {
    User getUserById(long userId);
}
