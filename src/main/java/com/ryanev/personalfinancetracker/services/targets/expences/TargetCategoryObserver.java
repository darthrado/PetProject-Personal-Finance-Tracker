package com.ryanev.personalfinancetracker.services.targets.expences;

import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import com.ryanev.personalfinancetracker.data.entities.Target;
import com.ryanev.personalfinancetracker.data.entities.TargetExpense;
import com.ryanev.personalfinancetracker.data.repo.targets.TargetsExpensesRepository;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.services.crud_observer.CrudChangeNotifier;
import com.ryanev.personalfinancetracker.services.crud_observer.CrudChangeObserver;
import com.ryanev.personalfinancetracker.services.crud_observer.SimpleCrudChangeObserver;
import com.ryanev.personalfinancetracker.services.targets.core.TargetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class TargetCategoryObserver extends SimpleCrudChangeObserver<MovementCategory> {

    TargetsService targetsService;

    TargetsExpensesRepository targetsExpensesRepository;

    @Autowired
    public TargetCategoryObserver(CrudChangeNotifier<MovementCategory> notifier,
                                  TargetsService targetsService,
                                  TargetsExpensesRepository targetsExpensesRepository) {
        super(notifier);
        this.targetsService = targetsService;
        this.targetsExpensesRepository = targetsExpensesRepository;
    }

    @Override
    public void notifyCreate(Collection<MovementCategory> categories) {
        categories.stream().forEach(category -> {
            try {
                createNewExpenseTarget(category);
            } catch (IncorrectUserIdException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void notifyUpdate(Collection<MovementCategory> categories) {
        //nothing to do in case of update
    }

    @Override
    public void notifyDelete(Collection<MovementCategory> category) {
        category.stream().forEach(this::deleteExpenseTarget);
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
