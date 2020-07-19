package com.ryanev.personalfinancetracker.services.implementation;

import com.ryanev.personalfinancetracker.dao.UserRepository;
import com.ryanev.personalfinancetracker.entities.User;
import com.ryanev.personalfinancetracker.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultUserService implements UserService {

    @Autowired
    private UserRepository userRepository;


    @Override
    public User getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow();
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }
}
