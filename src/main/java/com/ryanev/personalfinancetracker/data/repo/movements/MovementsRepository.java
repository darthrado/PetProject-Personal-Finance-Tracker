package com.ryanev.personalfinancetracker.data.repo.movements;

import com.ryanev.personalfinancetracker.data.entities.Movement;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface MovementsRepository extends CrudRepository<Movement,Long>, JpaSpecificationExecutor<Movement> {
    @Override
    List<Movement> findAll();

    List<Movement> findAllByUserId(Long userId);
    List<Movement> findAllByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"category"})
    List<Movement> findAll(Specification specification);

    @Query(value = "SELECT * " +
            "         FROM movements " +
            "        WHERE user_id = ? " +
            "          AND value_date BETWEEN ? AND ?",nativeQuery = true)
    List<Movement> findAllByUserIdAndPeriod(Long userId, LocalDate startDate, LocalDate endDate);
}
