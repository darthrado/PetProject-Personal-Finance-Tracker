package com.ryanev.personalfinancetracker.dto.movements;

import java.time.LocalDate;

public interface MovementViewDTO {
    Long getId();
    LocalDate getValueDate();
    Double getSignedAmount();
    String getName();
    String getCategoryName();
}
