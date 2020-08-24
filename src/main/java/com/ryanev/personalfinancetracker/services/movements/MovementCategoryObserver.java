package com.ryanev.personalfinancetracker.services.movements;

import com.ryanev.personalfinancetracker.data.entities.Movement;
import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import com.ryanev.personalfinancetracker.data.repo.categories.CategoriesRepository;
import com.ryanev.personalfinancetracker.data.repo.movements.MovementsRepository;
import com.ryanev.personalfinancetracker.services.crud_observer.CrudChangeNotifier;
import com.ryanev.personalfinancetracker.services.crud_observer.SimpleCrudChangeObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class MovementCategoryObserver extends SimpleCrudChangeObserver<MovementCategory> {

    MovementsRepository movementsRepository;
    CategoriesRepository categoriesRepository;
    //TODO use services?

    @Autowired
    protected MovementCategoryObserver(CrudChangeNotifier<MovementCategory> notifier,
                                       MovementsRepository movementsRepository,
                                       CategoriesRepository categoriesRepository) {
        super(notifier);
        this.movementsRepository = movementsRepository;
        this.categoriesRepository = categoriesRepository;
    }

    @Override
    public void notifyDelete(Collection<MovementCategory> persistedObjects) {
        persistedObjects.stream().forEach(category ->
                {
                    List<Movement> movementsToUpdate = movementsRepository.findAllByCategoryId(category.getId());
                    MovementCategory newCategoryForMovements = categoriesRepository.findById(category.getFallbackCategoryId()).orElseThrow();

                    movementsToUpdate.forEach(movement -> movement.setCategory(newCategoryForMovements));
                    movementsRepository.saveAll(movementsToUpdate);
                }
        );

    }
}
