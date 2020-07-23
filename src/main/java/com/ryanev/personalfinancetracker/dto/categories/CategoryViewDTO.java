package com.ryanev.personalfinancetracker.dto.categories;

public interface CategoryViewDTO {
    String getName();
    String getActive();
    String getEnableDisableText();
    String getEnableDisableLink();
    String getUpdateLink();
    String getDeleteLink();
    Boolean getFlagDefault();
}
