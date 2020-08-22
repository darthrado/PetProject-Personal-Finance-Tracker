package com.ryanev.personalfinancetracker.services.crud_observer;

import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class CrudChangeNotifierImpl<T> implements CrudChangeNotifier<T> {

    List<CrudChangeObserver<T>> observers;

    protected CrudChangeNotifierImpl() {
        this.observers = new ArrayList<>();
    }

    @Override
    public void notifyAllObservers(Collection<T> persistedObjects, NewState stateChange){
        switch (stateChange){
            case CREATE: observers.stream().forEach(categoryObserver -> categoryObserver.notifyCreate(persistedObjects)); break;
            case DELETE: observers.stream().forEach(categoryObserver -> categoryObserver.notifyDelete(persistedObjects)); break;
            case UPDATE: observers.stream().forEach(categoryObserver -> categoryObserver.notifyUpdate(persistedObjects)); break;
        }
    }

    @Override
    public void notifyAllObservers(T persistedObject, NewState stateChange) {
        notifyAllObservers(List.of(persistedObject),stateChange);
    }

    @Override
    public void addObserver(CrudChangeObserver<T> observer) {
        observers.add(observer);
    }
}
