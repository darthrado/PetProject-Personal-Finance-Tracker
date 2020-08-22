package com.ryanev.personalfinancetracker.services.categories;

import com.ryanev.personalfinancetracker.data.entities.User;
import com.ryanev.personalfinancetracker.exceptions.IncorrectCategoryIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.services.crud_observer.CrudChangeNotifier;
import com.ryanev.personalfinancetracker.services.crud_observer.SimpleCrudChangeObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class CategoryUserObserver extends SimpleCrudChangeObserver<User> {


    CategoriesService categoriesService;

    @Autowired
    protected CategoryUserObserver(CrudChangeNotifier<User> notifier,CategoriesService categoriesService) {
        super(notifier);
        this.categoriesService = categoriesService;
    }

    @Override
    public void notifyCreate(Collection<User> persistedObjects) {

        persistedObjects
                .stream()
                .forEach(user -> {
                    try {
                        categoriesService.createDefaultCategoriesForUser(user.getId());
                    }catch (IncorrectUserIdException e){
                        throw new RuntimeException("Incorrect user id passed");
                    }

                });
    }

    @Override
    public void notifyUpdate(Collection<User> persistedObjects) {

    }

    @Override
    public void notifyDelete(Collection<User> persistedObjects) {

        persistedObjects
                .stream()
                .forEach(user -> {
                    categoriesService.getCategoriesForUser(user.getId())
                            .stream()
                            .forEach(category -> {
                                try {
                                    categoriesService.deleteCategoryById(category.getId());
                                } catch (IncorrectCategoryIdException e) {
                                    throw new RuntimeException("Category doesn't exist????");
                                }
                            });//TODO do bulk

                });

    }
}
