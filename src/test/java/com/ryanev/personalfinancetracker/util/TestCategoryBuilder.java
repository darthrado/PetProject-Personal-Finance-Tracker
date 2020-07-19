package com.ryanev.personalfinancetracker.util;

import com.ryanev.personalfinancetracker.dao.CategoriesRepository;
import com.ryanev.personalfinancetracker.entities.MovementCategory;
import com.ryanev.personalfinancetracker.entities.User;

public class TestCategoryBuilder {
    private MovementCategory categoryToBuild;

    private TestCategoryBuilder(MovementCategory category){
        categoryToBuild = category;
    }

    public static TestCategoryBuilder createValidCategory(){

        MovementCategory newCategory = new MovementCategory();
        newCategory.setId(-999L);
        newCategory.setName("TestCateg123");
        newCategory.setUser(TestUserBuilder.createValidUser().build());

        return new TestCategoryBuilder(newCategory);
    }

    public TestCategoryBuilder withId (Long id){
        categoryToBuild.setId(id);

        return this;
    }

    public TestCategoryBuilder withName(String name){
        categoryToBuild.setName(name);

        return this;
    }
    public TestCategoryBuilder withUser(User user){
        categoryToBuild.setUser(user);

        return this;
    }

    public TestCategoryBuilder withDescription(String description){
        categoryToBuild.setDescription(description);

        return this;
    }

    public MovementCategory build(){
        return categoryToBuild;
    }


}
