package com.ryanev.personalfinancetracker.data.repo.targets;

import com.ryanev.personalfinancetracker.data.entities.TargetDetail;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TargetDetailsRepository extends CrudRepository<TargetDetail,Long> {

    List<TargetDetail> findAllByTargetId(Long targetId);
    void deleteAllByTargetIdIn(List<Long> targetId);
}
