package com.ryanev.personalfinancetracker.web.dto.categories;

public interface CategoryViewDTO {
    String getName();
    String getActive();
    String getEnableDisableText();
    String getEnableDisableLink();
    String getUpdateLink();
    String getDeleteLink();
    Boolean getFlagDefault();
}
