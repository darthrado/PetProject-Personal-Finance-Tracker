package com.ryanev.personalfinancetracker.services.targets.savings;

import com.ryanev.personalfinancetracker.exceptions.IncorrectTargetAmountException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.exceptions.TargetDataInvalidException;

import java.time.LocalDate;

public interface TargetSavingsService {
    Double getTargetSavingsAmount(Long userId, LocalDate date) throws IncorrectUserIdException, TargetDataInvalidException;
    void saveSavingsTargetForUser(Long userId, Double amount) throws IncorrectUserIdException, IncorrectTargetAmountException;
}
