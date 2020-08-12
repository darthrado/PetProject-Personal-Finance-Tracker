package com.ryanev.personalfinancetracker.services.targets.expences;

import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import com.ryanev.personalfinancetracker.data.entities.Target;
import com.ryanev.personalfinancetracker.data.entities.TargetExpense;
import com.ryanev.personalfinancetracker.data.repo.categories.CategoriesRepository;
import com.ryanev.personalfinancetracker.data.repo.targets.TargetsExpensesRepository;
import com.ryanev.personalfinancetracker.exceptions.IncorrectCategoryName;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.services.targets.core.TargetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class DefaultTargetCategorySyncService  implements TargetCategorySyncService {

    @Autowired
    CategoriesRepository categoriesRepository;

    @Autowired
    TargetsExpensesRepository targetsExpensesRepository;

    @Autowired
    TargetsService targetsService;

    @Override
    public void registerTargetForUserIdAndCategory(Long userId, String categoryName) throws IncorrectUserIdException, IncorrectCategoryName {

        MovementCategory category;

        try {
            category = categoriesRepository.findByUserIdAndName(userId,categoryName).orElseThrow();
        }catch (NoSuchElementException e){
            throw new IncorrectCategoryName();
        }

        registerTargetForUserIdAndCategory(userId,category);

    }

    private void registerTargetForUserIdAndCategory(Long userId, MovementCategory category) throws IncorrectUserIdException {

        Target newTarget = targetsService.createNewTargetForUser(userId);

        TargetExpense targetExpense = new TargetExpense();
        targetExpense.setTarget(newTarget);
        targetExpense.setCategory(category);

        targetsExpensesRepository.save(targetExpense);
    }

    @Override
    public void deleteTargetForUserIdAndCategoryName(Long userId, String categoryName) {
        List<TargetExpense> targetsForDelete = targetsExpensesRepository.getAllByUserId(userId)
                .stream()
                .filter(targetExpense -> targetExpense.getCategory().getName().equals(categoryName))
                .collect(Collectors.toList());

        targetsExpensesRepository.deleteAll(targetsForDelete);

        List<Long> targetIds = targetsForDelete.stream().map(TargetExpense::getTargetId).collect(Collectors.toList());
        targetsService.deleteTargetsByIds(targetIds);
    }

    @Override
    public void syncExpenseTargetsWithCategoriesForUser(Long userId) throws IncorrectUserIdException {

        removeTargetsForDeletedCategories(userId);

        createTargetsForCategoriesWithoutOne(userId);
    }

    private void createTargetsForCategoriesWithoutOne(Long userId) throws IncorrectUserIdException {
        List<MovementCategory> categoriesWithoutTargetEntries = categoriesRepository.findAllByUserId(userId)
                .stream()
                .filter(movementCategory ->  !targetsExpensesRepository.existsByCategoryId(movementCategory.getId()))
                .collect(Collectors.toList());

        for(MovementCategory category:categoriesWithoutTargetEntries) {
            registerTargetForUserIdAndCategory(userId,category);
        }
    }

    private void removeTargetsForDeletedCategories(Long userId){
        List<Long> expensesWithNoValidCategory =
                targetsExpensesRepository.getAllByUserId(userId)
                        .stream()
                        .filter(targetExpense -> targetExpense.getCategory()==null)
                        .map(TargetExpense::getTargetId)
                        .collect(Collectors.toList());

        targetsService.deleteTargetsByIds(expensesWithNoValidCategory);
    }


}
