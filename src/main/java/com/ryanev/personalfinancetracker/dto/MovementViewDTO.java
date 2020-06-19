package com.ryanev.personalfinancetracker.dto;

import java.util.Date;

public interface MovementViewDTO {
    Long getId();
    Date getValueDate();
    Double getSignedAmount();
    String getName();
    String getCategoryName();
}
