package com.ryanev.personalfinancetracker.services;

import com.ryanev.personalfinancetracker.entities.User;
import com.ryanev.personalfinancetracker.entities.UserCacheData;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;

import java.time.LocalDate;
import java.util.List;

public interface UserService {
    List<User> getAll();
    User getUserById(Long userId);

    UserCacheData getUserCache(Long userId) throws IncorrectUserIdException;
    void updateCacheWithMovementDate(Long userId, LocalDate movementDate);

    Boolean existsById(Long id);
}
