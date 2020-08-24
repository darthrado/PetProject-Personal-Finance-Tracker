package com.ryanev.personalfinancetracker.services.dto.users;

import java.time.LocalDate;

public class UserCacheDTO {
    Long userId;
    LocalDate minMovementDate;
    LocalDate maxMovementDate;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getMinMovementDate() {
        return minMovementDate;
    }

    public void setMinMovementDate(LocalDate minMovementDate) {
        this.minMovementDate = minMovementDate;
    }

    public LocalDate getMaxMovementDate() {
        return maxMovementDate;
    }

    public void setMaxMovementDate(LocalDate maxMovementDate) {
        this.maxMovementDate = maxMovementDate;
    }
}
