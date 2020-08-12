package com.ryanev.personalfinancetracker.util.user;

import com.ryanev.personalfinancetracker.data.entities.User;
import com.ryanev.personalfinancetracker.data.entities.UserCacheData;

import java.time.LocalDate;

public class TestUserCacheBuilder {
    final UserCacheData userCacheToBuild;

    private TestUserCacheBuilder(UserCacheData user){
        userCacheToBuild=user;
    }

    public static TestUserCacheBuilder createValidUser(){
        UserCacheData cacheData = new UserCacheData();
        cacheData.setUser(TestUserBuilder.createValidUser().build());
        cacheData.setUserId(cacheData.getUserId());
        cacheData.setMinMovementDate(LocalDate.of(2020, 1, 1));
        cacheData.setMaxMovementDate(LocalDate.of(2021,12,31));


        return new TestUserCacheBuilder(cacheData);
    }

    public TestUserCacheBuilder withUser(User user){
        userCacheToBuild.setUser(user);
        userCacheToBuild.setUserId(user.getId());

        return this;
    }
    public TestUserCacheBuilder withMinMovementDate(LocalDate minMovementDate){
        userCacheToBuild.setMinMovementDate(minMovementDate);

        return this;
    }
    public TestUserCacheBuilder withMaxMovementDate(LocalDate maxMovementDate){
        userCacheToBuild.setMaxMovementDate(maxMovementDate);

        return this;
    }

    public UserCacheData build(){
        return userCacheToBuild;
    }
}
