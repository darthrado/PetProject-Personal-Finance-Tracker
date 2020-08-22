package com.ryanev.personalfinancetracker.services.users;

import com.ryanev.personalfinancetracker.data.entities.UserAuth;
import com.ryanev.personalfinancetracker.data.repo.users.UserAuthRepository;
import com.ryanev.personalfinancetracker.data.repo.users.UserCacheRepository;
import com.ryanev.personalfinancetracker.data.repo.users.UserRepository;
import com.ryanev.personalfinancetracker.data.entities.User;
import com.ryanev.personalfinancetracker.data.entities.UserCacheData;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.exceptions.UserAlreadyExistsException;
import com.ryanev.personalfinancetracker.services.crud_observer.CrudChangeNotifier;
import com.ryanev.personalfinancetracker.web.dto.security.UserAccountDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DefaultUserService implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private UserCacheRepository userCacheRepository;

    @Autowired
    private UserNotifier userNotifier;

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
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }

    @Override
    public Boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    @Transactional
    public void register(UserAccountDTO userAccountDTO) throws UserAlreadyExistsException {

        if(userRepository.existsByUsername(userAccountDTO.getUsername())){
            throw new UserAlreadyExistsException();
        }

        User newUser = new User();
        newUser.setUsername(userAccountDTO.getUsername());
        userRepository.save(newUser);

        UserAuth newUserAuth = new UserAuth();
        newUserAuth.setUser(newUser);
        newUserAuth.setUserId(newUser.getId());
        newUserAuth.setPassword(userAccountDTO.getPassword());
        newUserAuth.setEmail(userAccountDTO.getEmail());
        newUserAuth.setActive(true);
        newUserAuth.setRole("ROLE_USER");
        userAuthRepository.save(newUserAuth);

        UserCacheData newUserCache = new UserCacheData();
        newUserCache.setUser(newUser);
        newUserCache.setUserId(newUser.getId());
        userCacheRepository.save(newUserCache);

        userNotifier.notifyAllObservers(newUser, CrudChangeNotifier.NewState.CREATE);
    }


}
