package com.ryanev.personalfinancetracker.services.movements;

import com.ryanev.personalfinancetracker.services.dto.movements.MovementSearchFilter;

import java.time.LocalDate;

public class MovementSearchFilterImpl implements MovementSearchFilterBuilder {

    private MovementSearchFilter movementSearchFIlter;

    private MovementSearchFilterImpl() {
        movementSearchFIlter = new MovementSearchFilter();
    }

    public static MovementSearchFilterBuilder createFilterForUser(Long userId) {
        MovementSearchFilterBuilder newFilter = new MovementSearchFilterImpl();

        return newFilter.withUserId(userId);
    }

    @Override
    public MovementSearchFilterBuilder withUserId(Long userId) {
        movementSearchFIlter.setUserId(userId);
        return this;
    }

    @Override
    public MovementSearchFilterBuilder withUserName(String userName) {
        movementSearchFIlter.setUserName(userName);
        return this;
    }

    @Override
    public MovementSearchFilter build() {
        return movementSearchFIlter;
    }

    @Override
    public MovementSearchFilterBuilder withCategoryName(String categoryName) {
        movementSearchFIlter.setCategoryName(categoryName);
        return this;
    }

    @Override
    public MovementSearchFilterBuilder withAmountFrom(Double amountFrom) {
        movementSearchFIlter.setAmountFrom(amountFrom);
        return this;
    }

    @Override
    public MovementSearchFilterBuilder withAmountTo(Double amountTo) {
        movementSearchFIlter.setAmountTo(amountTo);
        return this;
    }

    @Override
    public MovementSearchFilterBuilder withName(String name) {
        movementSearchFIlter.setName(name);
        return this;
    }

    @Override
    public MovementSearchFilterBuilder withDateFrom(LocalDate dateFrom) {
        movementSearchFIlter.setDateFrom(dateFrom);
        return this;
    }

    @Override
    public MovementSearchFilterBuilder withDateTo(LocalDate dateTo) {
        movementSearchFIlter.setDateTo(dateTo);
        return this;
    }
}
