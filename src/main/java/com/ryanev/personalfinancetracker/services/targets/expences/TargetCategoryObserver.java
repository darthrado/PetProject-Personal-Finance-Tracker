package com.ryanev.personalfinancetracker.services.targets.expences;

import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import com.ryanev.personalfinancetracker.data.entities.Target;
import com.ryanev.personalfinancetracker.data.entities.TargetExpense;
import com.ryanev.personalfinancetracker.data.repo.targets.TargetsExpensesRepository;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.services.categories.CategoryObserver;
import com.ryanev.personalfinancetracker.services.targets.core.TargetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TargetCategoryObserver implements CategoryObserver {

    TargetsService targetsService;

    TargetsExpensesRepository targetsExpensesRepository;

    @Autowired
    public TargetCategoryObserver(TargetsService targetsService, TargetsExpensesRepository targetsExpensesRepository) {
        this.targetsService = targetsService;
        this.targetsExpensesRepository = targetsExpensesRepository;
    }

    @Override
    @Transactional
    public void notify(MovementCategory category, NewState newState) {
        switch (newState){
            case CREATE:
                try {
                    createNewExpenseTarget(category);
                } catch (IncorrectUserIdException e) {
                    e.printStackTrace();
                }
                break;
            case DELETE:
                deleteExpenseTarget(category); break;
        }
    }

    private void createNewExpenseTarget(MovementCategory category) throws IncorrectUserIdException {
        Target newTarget = targetsService.createNewTargetForUser(category.getUser().getId());

        TargetExpense targetExpense = new TargetExpense();
        targetExpense.setTarget(newTarget);
        targetExpense.setCategory(category);

        targetsExpensesRepository.save(targetExpense);
    }

    private void deleteExpenseTarget(MovementCategory category){
        Optional<TargetExpense> targetForDelete = targetsExpensesRepository.getByCategoryId(category.getId());

        if(!targetForDelete.isEmpty()){
            targetsExpensesRepository.delete(targetForDelete.get());
            targetsService.deleteTargetsByIds(List.of(targetForDelete.get().getTargetId()));
        }
    }


}
