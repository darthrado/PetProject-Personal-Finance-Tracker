package com.ryanev.personalfinancetracker.services.categories;

import com.ryanev.personalfinancetracker.data.entities.MovementCategory;

public interface CategoryObserver {
    enum NewState{CREATE,UPDATE,DELETE};

    public void notify(MovementCategory category, NewState newState);
}
