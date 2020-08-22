package com.ryanev.personalfinancetracker.data.repo.targets;

import com.ryanev.personalfinancetracker.data.entities.TargetExpense;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;


import java.util.List;
import java.util.Optional;

public interface TargetsExpensesRepository extends CrudRepository<TargetExpense,Long> {

    @Query(value = "SELECT e FROM TargetExpense e JOIN e.target t JOIN t.user u WHERE u.id = :userId")
    @EntityGraph(attributePaths = {"category","target"})
    List<TargetExpense> getAllByUserId(Long userId);

    Boolean existsByCategoryId(Long id);

    Optional<TargetExpense> getByCategoryId(Long categoryId);

}
