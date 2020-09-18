package com.ryanev.personalfinancetracker.web.dto.categories;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CategoryApiDTO {
    @NotBlank
    String name;
    @NotNull
    Boolean flagActive;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getFlagActive() {
        return flagActive;
    }

    public void setFlagActive(Boolean flagActive) {
        this.flagActive = flagActive;
    }
}
