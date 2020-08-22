package com.ryanev.personalfinancetracker.services.crud_observer;

import java.util.Collection;

public interface CrudChangeObserver<T> {

    void notifyCreate(Collection<T> persistedObjects);
    void notifyUpdate(Collection<T> persistedObjects);
    void notifyDelete(Collection<T> persistedObjects);

}
