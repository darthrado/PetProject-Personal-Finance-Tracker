package com.ryanev.personalfinancetracker.services.categories;

import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryChangeNotifierImpl implements CategoryChangeNotifier{

    List<CategoryObserver> observers;

    public CategoryChangeNotifierImpl() {
        this.observers = new ArrayList<>();
    }

    @Override
    public void notifyAllObservers(MovementCategory category, CategoryObserver.NewState stateChange) {
        observers.stream().forEach(categoryObserver -> categoryObserver.notify(category,stateChange));
    }

    @Override
    public void addObserver(CategoryObserver observer) {
        observers.add(observer);
    }
}
