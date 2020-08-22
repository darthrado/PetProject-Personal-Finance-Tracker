package com.ryanev.personalfinancetracker.services.crud_observer;

import java.util.Collection;

public abstract class SimpleCrudChangeObserver<T> implements CrudChangeObserver<T> {
    protected SimpleCrudChangeObserver(CrudChangeNotifier<T> notifier) {
        notifier.addObserver(this);
    }

    @Override
    public void notifyCreate(Collection<T> persistedObjects) {

    }

    @Override
    public void notifyUpdate(Collection<T> persistedObjects) {

    }

    @Override
    public void notifyDelete(Collection<T> persistedObjects) {

    }
}
