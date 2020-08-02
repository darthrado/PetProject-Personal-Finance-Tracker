package com.ryanev.personalfinancetracker.dto.categories;

public interface CategoryFormDTO {
    Long getId();
    void setId(Long id);

    String getName();
    void setName(String name);

    String getDescription();
    void setDescription(String description);

    Long getFallbackCategoryId();
    void setFallbackCategoryId(Long id);
}
