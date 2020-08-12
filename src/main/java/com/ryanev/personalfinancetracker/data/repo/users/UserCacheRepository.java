package com.ryanev.personalfinancetracker.data.repo.users;

import com.ryanev.personalfinancetracker.data.entities.UserCacheData;
import org.springframework.data.repository.CrudRepository;

public interface UserCacheRepository extends CrudRepository<UserCacheData,Long> {
}
