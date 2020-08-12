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
        newUser.setEmail("test_mail@gmail.com");
        newUser.setPassword("more_secure_than_nap");

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

    public TestUserBuilder withPassword(String password){
        userToBuild.setPassword(password);

        return this;
    }

    public TestUserBuilder withEmail(String email){
        userToBuild.setEmail(email);

        return this;
    }

    public User build(){
        return userToBuild;
    }
}
