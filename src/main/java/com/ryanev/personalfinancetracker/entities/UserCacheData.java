package com.ryanev.personalfinancetracker.entities;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "users_cache_data")
public class UserCacheData {

    @Id
    private Long userId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Nullable
    private LocalDate minMovementDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Nullable
    private LocalDate maxMovementDate;

    @OneToOne
    @MapsId
    private User user;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Nullable
    public LocalDate getMinMovementDate() {
        return minMovementDate;
    }

    public void setMinMovementDate(@Nullable LocalDate minMovementDate) {
        this.minMovementDate = minMovementDate;
    }

    @Nullable
    public LocalDate getMaxMovementDate() {
        return maxMovementDate;
    }

    public void setMaxMovementDate(@Nullable LocalDate maxMovementDate) {
        this.maxMovementDate = maxMovementDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
