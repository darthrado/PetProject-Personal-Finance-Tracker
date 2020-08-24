package com.ryanev.personalfinancetracker.services.dto.categories;

public class CategoryDTO {
    Long id;
    String name;
    String description;
    Boolean flagActive;
    Long userId;
    Long fallbackCategoryId;

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

    public Boolean getFlagActive() {
        return flagActive;
    }

    public void setFlagActive(Boolean flagActive) {
        this.flagActive = flagActive;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFallbackCategoryId() {
        return fallbackCategoryId;
    }

    public void setFallbackCategoryId(Long fallbackCategoryId) {
        this.fallbackCategoryId = fallbackCategoryId;
    }
}
