package com.ryanev.personalfinancetracker.data.repo.targets;

import com.ryanev.personalfinancetracker.data.entities.Target;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TargetsRepository extends CrudRepository<Target,Long> {

    void deleteAllByIdIn(List<Long> id);
}
