package com.ryanev.personalfinancetracker.web.dto.movements;

import java.time.LocalDate;

public class MovementViewDTO {

    private LocalDate valueDate;
    private Double signedAmount;
    private String name;
    private String categoryName;
    private String updateLink;
    private String deleteLink;

    public LocalDate getValueDate() {
        return valueDate;
    }

    public void setValueDate(LocalDate valueDate) {
        this.valueDate = valueDate;
    }

    public Double getSignedAmount() {
        return signedAmount;
    }

    public void setSignedAmount(Double signedAmount) {
        this.signedAmount = signedAmount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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
}
