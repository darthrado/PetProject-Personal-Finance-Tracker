package com.ryanev.personalfinancetracker.util.user;

import com.ryanev.personalfinancetracker.data.entities.User;

public class TestUserBuilder {
    final User userToBuild;

    private TestUserBuilder(User user){
        userToBuild=user;
    }

    public static TestUserBuilder createValidUser(){
        User newUser = new User();
        newUser.setId(777L);
        newUser.setUsername("TestUser");

        return new TestUserBuilder(newUser);
    }

    public TestUserBuilder withId(Long id){
        userToBuild.setId(id);

        return this;
    }

    public TestUserBuilder withUsername(String username){
        userToBuild.setUsername(username);

        return this;
    }

    public User build(){
        return userToBuild;
    }
}
