package com.ryanev.personalfinancetracker.web.dto.movements;

import java.time.LocalDate;

public interface MovementFormDTO {


    Long getCategoryId();
    void setCategoryId(Long categoryId);

    Long getId();
    void setId(Long id);

    Double getUnsignedAmount();
    void setUnsignedAmount(Double amount);

    LocalDate getValueDate();
    void setValueDate(LocalDate valueDate);

    String getName();
    void setName(String name);

    Boolean getFlagAmountPositive();
    void setFlagAmountPositive(Boolean flagAmountPositive);

    String getDescription();
    void setDescription(String description);

    //Movement build();
}
