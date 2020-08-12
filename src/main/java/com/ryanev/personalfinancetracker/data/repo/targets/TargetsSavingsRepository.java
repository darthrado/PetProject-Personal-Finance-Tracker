package com.ryanev.personalfinancetracker.data.repo.targets;

import com.ryanev.personalfinancetracker.data.entities.TargetSavings;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface TargetsSavingsRepository extends CrudRepository<TargetSavings,Long> {

    @Query(value = "SELECT targets_savings.* " +
            "FROM targets, targets_savings " +
            "WHERE targets.id = targets_savings.target_id", nativeQuery = true)
    Optional<TargetSavings> getByUserId(Long userId);

}
