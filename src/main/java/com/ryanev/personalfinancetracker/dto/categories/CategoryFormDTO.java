package com.ryanev.personalfinancetracker.dto.categories;

import java.time.LocalDate;

public interface CategoryFormDTO {
    Long getId();
    void setId(Long id);

    String getName();
    void setName(String name);

    String getDescription();
    void setDescription(String description);

    Boolean getFlagActive();
    void setFlagActive(Boolean flagActive);
}
