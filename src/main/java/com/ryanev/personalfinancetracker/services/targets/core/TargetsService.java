package com.ryanev.personalfinancetracker.services.targets.core;

import com.ryanev.personalfinancetracker.data.entities.Target;
import com.ryanev.personalfinancetracker.data.entities.TargetDetail;
import com.ryanev.personalfinancetracker.exceptions.IncorrectTargetAmountException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectTargetIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;

import java.time.LocalDate;
import java.util.List;

public interface TargetsService {

    TargetDetail getLatestDetailForTargetAndDate(Long targetId, LocalDate date) throws IncorrectTargetIdException;

    Target createNewTargetForUser(Long userId) throws IncorrectUserIdException;

    void saveTargetAmount(Long targetId, Double amount) throws IncorrectTargetIdException, IncorrectTargetAmountException;

    void deleteTargetsByIds(List<Long> listOfTargetIds);
}
