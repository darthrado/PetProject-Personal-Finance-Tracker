package com.ryanev.personalfinancetracker.dto.categories.implementations;

import com.ryanev.personalfinancetracker.dto.categories.CategoryFormDTO;
import com.ryanev.personalfinancetracker.entities.MovementCategory;

public class DefaultCategoryFormDTO implements CategoryFormDTO {

    Long id;
    String name;
    String description;
    Long fallbackCategoryId;

    public DefaultCategoryFormDTO() {
    }

    public DefaultCategoryFormDTO(MovementCategory category) {
        if(category != null) {
            id = category.getId();
            name = category.getName();
            description = category.getDescription();
            fallbackCategoryId = category.getFallbackCategoryId();

        }
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id=id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name=name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Long getFallbackCategoryId() {
        return fallbackCategoryId;
    }

    @Override
    public void setFallbackCategoryId(Long id) {
        this.fallbackCategoryId = id;
    }
}
