package com.ryanev.personalfinancetracker.data.repo.targets;

import com.ryanev.personalfinancetracker.data.entities.TargetSavings;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface TargetsSavingsRepository extends CrudRepository<TargetSavings,Long> {

//    @Query(value = "SELECT targets_savings.* " +
//            "FROM targets, targets_savings " +
//            "WHERE targets.id = targets_savings.target_id "+
//            "AND targets.user_id = ?", nativeQuery = true)
    @Query(value = "SELECT s FROM TargetSavings s JOIN s.target t JOIN t.user u WHERE u.id = :userId")
    @EntityGraph(attributePaths = {"target"})
    Optional<TargetSavings> getByUserId(Long userId);

}
