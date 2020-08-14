package com.ryanev.personalfinancetracker.services.targets.expences;

import com.ryanev.personalfinancetracker.exceptions.IncorrectTargetAmountException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectTargetIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.services.dto.targets.TargetExpensesAndAmountDTO;

import java.time.LocalDate;
import java.util.List;

public interface TargetExpensesService {
    List<TargetExpensesAndAmountDTO> getExpenseTargetNameAndAmount(Long userId, LocalDate date) throws IncorrectUserIdException;
    void saveExpensesTargetAmount(Long targetId,Double amount) throws IncorrectTargetIdException, IncorrectTargetAmountException;
}
