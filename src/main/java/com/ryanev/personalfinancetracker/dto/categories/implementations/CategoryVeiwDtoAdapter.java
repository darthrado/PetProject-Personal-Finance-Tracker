package com.ryanev.personalfinancetracker.dto.categories.implementations;

import com.ryanev.personalfinancetracker.dto.categories.CategoryViewDTO;
import com.ryanev.personalfinancetracker.entities.MovementCategory;

public class CategoryVeiwDtoAdapter implements CategoryViewDTO {

    MovementCategory movementCategory;

    public CategoryVeiwDtoAdapter(MovementCategory movementCategory) {
        this.movementCategory = movementCategory;
    }

    @Override
    public Long getId() {
        return movementCategory.getId();
    }

    @Override
    public String getName() {
        return movementCategory.getName();
    }
}
