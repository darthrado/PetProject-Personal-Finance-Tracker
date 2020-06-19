package com.ryanev.personalfinancetracker.dto;

import java.util.Date;

public interface MovementFormDTO {


    Long getCategoryId();
    void setCategoryId(Long categoryId);

    Long getId();
    void setId(Long id);

    Double getUnsignedAmount();
    void setUnsignedAmount(Double amount);

    Date getValueDate();
    void setValueDate(Date valueDate);

    String getName();
    void setName(String name);

    Boolean getFlagAmountPositive();
    void setFlagAmountPositive(Boolean flagAmountPositive);

    String getDescription();
    void setDescription(String description);
}
