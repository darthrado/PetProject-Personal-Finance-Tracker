package com.ryanev.personalfinancetracker.dao;

import com.ryanev.personalfinancetracker.entities.UserCacheData;
import org.springframework.data.repository.CrudRepository;

public interface UserCacheRepository extends CrudRepository<UserCacheData,Long> {
}
