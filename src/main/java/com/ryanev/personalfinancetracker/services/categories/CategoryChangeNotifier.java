package com.ryanev.personalfinancetracker.services.categories;

import com.ryanev.personalfinancetracker.data.entities.MovementCategory;

public interface CategoryChangeNotifier {
    void notifyAllObservers(MovementCategory category,CategoryObserver.NewState stateChange);
    void addObserver(CategoryObserver observer);
}
