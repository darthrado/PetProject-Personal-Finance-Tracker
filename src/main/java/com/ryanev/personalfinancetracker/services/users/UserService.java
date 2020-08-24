package com.ryanev.personalfinancetracker.services.users;

import com.ryanev.personalfinancetracker.data.entities.User;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.exceptions.UserAlreadyExistsException;
import com.ryanev.personalfinancetracker.services.dto.users.UserCacheDTO;
import com.ryanev.personalfinancetracker.web.dto.security.UserAccountDTO;

import java.time.LocalDate;
import java.util.List;

public interface UserService {
    List<User> getAll();
    User getUserById(Long userId);
    User getUserByUsername(String username);

    UserCacheDTO getUserCache(Long userId) throws IncorrectUserIdException;
    void updateCacheWithMovementDate(Long userId, LocalDate movementDate);

    Boolean existsById(Long id);

    void register(UserAccountDTO userAccountDTO) throws UserAlreadyExistsException;
}
