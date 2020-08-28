package com.ryanev.personalfinancetracker.web.dto.categories;

import com.ryanev.personalfinancetracker.data.entities.MovementCategory;

import javax.validation.constraints.NotEmpty;

public class CategoryFormDTO{
    Long id;
    @NotEmpty
    String name;
    String description;
    Long fallbackCategoryId;

    public CategoryFormDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getFallbackCategoryId() {
        return fallbackCategoryId;
    }

    public void setFallbackCategoryId(Long fallbackCategoryId) {
        this.fallbackCategoryId = fallbackCategoryId;
    }
}
