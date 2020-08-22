package com.ryanev.personalfinancetracker.data.repo.users;

import com.ryanev.personalfinancetracker.data.entities.UserAuth;
import org.springframework.data.repository.CrudRepository;

public interface UserAuthRepository extends CrudRepository<UserAuth,Long> {
}
