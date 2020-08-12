package com.ryanev.personalfinancetracker.services.implementation;

import com.ryanev.personalfinancetracker.data.repo.users.UserCacheRepository;
import com.ryanev.personalfinancetracker.data.repo.users.UserRepository;
import com.ryanev.personalfinancetracker.data.entities.User;
import com.ryanev.personalfinancetracker.data.entities.UserCacheData;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DefaultUserService implements UserService {

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private UserCacheRepository userCacheRepository;

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow();
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public UserCacheData getUserCache(Long userId) throws IncorrectUserIdException {
        try {
            return userCacheRepository.findById(userId).orElseThrow();
        }catch (NoSuchElementException e){
            if (existsById(userId)){
                UserCacheData newCache = new UserCacheData();
                newCache.setUser(getUserById(userId));
                return newCache;
            }
            else {
                throw new IncorrectUserIdException();
            }
        }

    }

    @Override
    public void updateCacheWithMovementDate(Long userId, LocalDate movementDate) {
        UserCacheData cacheData = userCacheRepository.findById(userId).orElse(new UserCacheData());
        Boolean flagPersist = false;

        if (cacheData.getMinMovementDate() == null || cacheData.getMinMovementDate().isAfter(movementDate)){
            cacheData.setMinMovementDate(movementDate);
            flagPersist=true;
        }
        if(cacheData.getMaxMovementDate() == null || cacheData.getMaxMovementDate().isBefore(movementDate)){
            cacheData.setMaxMovementDate(movementDate);
            flagPersist=true;
        }
        if (cacheData.getUserId() == null){
            cacheData.setUser(getUserById(userId));
            flagPersist=true;
        }
        if (flagPersist){
            userCacheRepository.save(cacheData);
        }
    }

    @Override
    public Boolean existsById(Long id) {
        return userRepository.existsById(id);
    }
}
