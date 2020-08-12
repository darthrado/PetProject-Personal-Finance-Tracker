package com.ryanev.personalfinancetracker.dto.categories.implementations;

import com.ryanev.personalfinancetracker.dto.categories.CategoryViewDTO;
import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import org.springframework.web.util.UriComponentsBuilder;

public class CategoryVeiwDtoConcrete implements CategoryViewDTO {

    private final String name;
    private final String active;
    private final String enableDisableText;
    private final String enableDisableLink;
    private final String updateLink;
    private final String deleteLink;
    private final Boolean flagDefault;

    private final String ENABLE = "Enable";
    private final String DISABLE = "Disable";
    private final String ACTIVE = "Active";
    private final String DISABLED = "Disabled";


    public CategoryVeiwDtoConcrete(MovementCategory movementCategory, Boolean flagDefault, String baseUri) {
        name = movementCategory.getName();
        active = movementCategory.getFlagActive()?ACTIVE:DISABLED;

        enableDisableText = movementCategory.getFlagActive()?DISABLE:ENABLE;
        enableDisableLink = UriComponentsBuilder.fromUriString(baseUri.concat("/change_status"))
                .queryParam("id",movementCategory.getId())
                .queryParam("enable",!movementCategory.getFlagActive())
                .build()
                .toUriString();

        updateLink = UriComponentsBuilder.fromUriString(baseUri.concat("/update"))
                .queryParam("id",movementCategory.getId()).build().toUriString();

        deleteLink = UriComponentsBuilder.fromUriString(baseUri.concat("/delete"))
                .queryParam("id",movementCategory.getId()).build().toUriString();

        this.flagDefault=flagDefault;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getActive() {
        return active;
    }

    @Override
    public String getEnableDisableText() {
        return enableDisableText;
    }

    @Override
    public String getEnableDisableLink() {
        return enableDisableLink;
    }

    @Override
    public String getUpdateLink() {
        return updateLink;
    }

    @Override
    public String getDeleteLink() {
        return deleteLink;
    }

    @Override
    public Boolean getFlagDefault() {
        return flagDefault;
    }
}
