package com.ryanev.personalfinancetracker.web.dto.categories;

public class CategoryViewDTO {

    private String name;
    private String active;
    private String enableDisableText;
    private String enableDisableLink;
    private String updateLink;
    private String deleteLink;
    private Boolean flagDefault;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getEnableDisableText() {
        return enableDisableText;
    }

    public void setEnableDisableText(String enableDisableText) {
        this.enableDisableText = enableDisableText;
    }

    public String getEnableDisableLink() {
        return enableDisableLink;
    }

    public void setEnableDisableLink(String enableDisableLink) {
        this.enableDisableLink = enableDisableLink;
    }

    public String getUpdateLink() {
        return updateLink;
    }

    public void setUpdateLink(String updateLink) {
        this.updateLink = updateLink;
    }

    public String getDeleteLink() {
        return deleteLink;
    }

    public void setDeleteLink(String deleteLink) {
        this.deleteLink = deleteLink;
    }

    public Boolean getFlagDefault() {
        return flagDefault;
    }

    public void setFlagDefault(Boolean flagDefault) {
        this.flagDefault = flagDefault;
    }
}
