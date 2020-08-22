package com.ryanev.personalfinancetracker.services.crud_observer;

import java.util.Collection;

public interface CrudChangeNotifier<T> {

    enum NewState{CREATE,UPDATE,DELETE};

    void notifyAllObservers(Collection<T> persistedObjects, NewState stateChange);
    void notifyAllObservers(T persistedObject, NewState stateChange);
    void addObserver(CrudChangeObserver<T> observer);

}
