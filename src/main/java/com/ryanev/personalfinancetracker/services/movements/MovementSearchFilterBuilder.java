package com.ryanev.personalfinancetracker.services.movements;

import com.ryanev.personalfinancetracker.services.dto.movements.MovementSearchFilter;

import java.time.LocalDate;

public interface MovementSearchFilterBuilder {
    MovementSearchFilterBuilder withCategoryName(String categoryName);
    MovementSearchFilterBuilder withAmountFrom(Double amountFrom);
    MovementSearchFilterBuilder withAmountTo(Double amountTo);
    MovementSearchFilterBuilder withName(String name);
    MovementSearchFilterBuilder withDateFrom(LocalDate dateFrom);
    MovementSearchFilterBuilder withDateTo(LocalDate dateTo);
    MovementSearchFilterBuilder withUserId(Long userId);
    MovementSearchFilterBuilder withUserName(String userName);
    MovementSearchFilter build();
}

