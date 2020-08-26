package com.ryanev.personalfinancetracker.util;

import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import com.ryanev.personalfinancetracker.data.entities.User;
import com.ryanev.personalfinancetracker.services.dto.categories.CategoryDTO;
import com.ryanev.personalfinancetracker.util.user.TestUserBuilder;

public class TestCategoryBuilder {
    private final MovementCategory categoryToBuild;

    private TestCategoryBuilder(MovementCategory category){
        categoryToBuild = category;
    }

    public static TestCategoryBuilder createValidCategory(){

        MovementCategory newCategory = new MovementCategory();
        newCategory.setId(-999L);
        newCategory.setName("TestCateg123");
        newCategory.setFlagActive(true);
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

    public TestCategoryBuilder withFlagActive(Boolean flagActive){
        categoryToBuild.setFlagActive(flagActive);

        return this;
    }
    public TestCategoryBuilder withStatus(String status){
        switch (status){
            case "Active": categoryToBuild.setFlagActive(true); break;
            case "Disabled": categoryToBuild.setFlagActive(false); break;
            default: throw new RuntimeException("invalid value");
        }

        return this;
    }

    public TestCategoryBuilder withFallbackCategoryId(Long categoryId){
        categoryToBuild.setFallbackCategoryId(categoryId);

        return this;
    }

    public MovementCategory build(){
        return categoryToBuild;
    }

    public CategoryDTO buildDTO(){
        CategoryDTO dto = new CategoryDTO();
        dto.setUserId(categoryToBuild.getUser().getId());
        dto.setName(categoryToBuild.getName());
        dto.setDescription(categoryToBuild.getDescription());
        dto.setId(categoryToBuild.getId());
        dto.setFallbackCategoryId(categoryToBuild.getFallbackCategoryId());
        dto.setFlagActive(categoryToBuild.getFlagActive());

        return dto;
    }


}
