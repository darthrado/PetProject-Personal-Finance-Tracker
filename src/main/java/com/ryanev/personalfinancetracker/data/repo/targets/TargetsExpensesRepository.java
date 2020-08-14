package com.ryanev.personalfinancetracker.data.repo.targets;

import com.ryanev.personalfinancetracker.data.entities.TargetExpense;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;


import java.util.List;
import java.util.Optional;

public interface TargetsExpensesRepository extends CrudRepository<TargetExpense,Long> {

    @Query(value = "SELECT targets_expenses.* " +
            "FROM targets, targets_expenses " +
            "WHERE targets.id = targets_expenses.target_id", nativeQuery = true)
    List<TargetExpense> getAllByUserId(Long userId);

    Boolean existsByCategoryId(Long id);

    Optional<TargetExpense> getByCategoryId(Long categoryId);

}
