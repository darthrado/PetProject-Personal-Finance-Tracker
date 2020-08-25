package com.ryanev.personalfinancetracker.services.users;

import com.ryanev.personalfinancetracker.data.entities.Movement;
import com.ryanev.personalfinancetracker.services.crud_observer.CrudChangeNotifier;
import com.ryanev.personalfinancetracker.services.crud_observer.SimpleCrudChangeObserver;
import com.ryanev.personalfinancetracker.services.movements.MovementChangeNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;

@Service
public class UserCacheMovementObserver extends SimpleCrudChangeObserver<Movement>  {

    UserService userService;

    @Autowired
    public UserCacheMovementObserver(MovementChangeNotifier notifier,UserService service) {
        super(notifier);
        userService = service;
    }

    @Override
    public void notifyCreate(Collection<Movement> persistedObjects) {
        processCollection(persistedObjects);
    }

    @Override
    public void notifyUpdate(Collection<Movement> persistedObjects) {
        processCollection(persistedObjects);
    }

    @Override
    public void notifyDelete(Collection<Movement> persistedObjects) {
        //processCollection(persistedObjects);
    }


    private void processCollection(Collection<Movement> movements){
        //Note: we're making an assumption that we won't have a bulk insert for different users
        Long userId=null;
        LocalDate maxDate=null;
        LocalDate minDate=null;

        for (Movement movement:movements) {
            if (userId==null){
                userId=movement.getUser().getId();
            }
            if (maxDate==null||maxDate.isBefore(movement.getValueDate())){
                maxDate=movement.getValueDate();
            }
            if(minDate==null||minDate.isAfter(movement.getValueDate())){
                minDate=movement.getValueDate();
            }
        }

        userService.updateCacheWithMovementDate(userId,maxDate);
        if(!minDate.isEqual(maxDate)){
            userService.updateCacheWithMovementDate(userId,minDate);
        }

    }

    private void callCacheService(Long userId, LocalDate valueDate){
        userService.updateCacheWithMovementDate(userId,valueDate);
    }

}
