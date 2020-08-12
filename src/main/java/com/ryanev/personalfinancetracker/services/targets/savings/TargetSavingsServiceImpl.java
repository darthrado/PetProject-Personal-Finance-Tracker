package com.ryanev.personalfinancetracker.services.targets.savings;

import com.ryanev.personalfinancetracker.data.entities.TargetSavings;
import com.ryanev.personalfinancetracker.data.repo.targets.TargetsSavingsRepository;
import com.ryanev.personalfinancetracker.exceptions.IncorrectTargetAmountException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectTargetIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.services.UserService;
import com.ryanev.personalfinancetracker.services.targets.core.TargetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.NoSuchElementException;

@Service
public class TargetSavingsServiceImpl implements TargetSavingsService {

    @Autowired
    UserService userService;

    @Autowired
    TargetsService targetsService;

    @Autowired
    TargetsSavingsRepository targetsSavingsRepository;

    @Override
    public void saveSavingsTargetForUser(Long userId, Double amount) throws IncorrectUserIdException, IncorrectTargetAmountException {
        if (!userService.existsById(userId)){
            throw new IncorrectUserIdException();
        }
        if (amount!=null&&amount<0){
            throw new IncorrectTargetAmountException("Amount cannot be negative");
        }

        TargetSavings targetSavings;

        targetSavings = getTargetSavingsAndInitializeIfMissing(userId);

        try {
            targetsService.saveTargetAmount(targetSavings.getTargetId(),amount);
        } catch (IncorrectTargetIdException e){
            throw new RuntimeException("Existence as you know it is over!");
        }
    }

    @Override
    public Double getTargetSavingsAmount(Long userId, LocalDate date) throws IncorrectUserIdException, IncorrectTargetIdException {
        if (!userService.existsById(userId)){
            throw new IncorrectUserIdException();
        }

        TargetSavings targetSavings;

        targetSavings = getTargetSavingsAndInitializeIfMissing(userId);

        return targetsService.getLatestDetailForTargetAndDate(targetSavings.getTargetId(),date).getAmount();
    }

    private TargetSavings initializeTargetSavingsData(Long userId) throws IncorrectUserIdException {

        TargetSavings newSavings = new TargetSavings();
        newSavings.setTarget(targetsService.createNewTargetForUser(userId));
        targetsSavingsRepository.save(newSavings);

        return newSavings;
    }

    private TargetSavings getTargetSavingsAndInitializeIfMissing(Long userId) throws IncorrectUserIdException {
        TargetSavings targetSavings;
        try{
            targetSavings = targetsSavingsRepository.getByUserId(userId).orElseThrow();
        }catch (NoSuchElementException e){
            targetSavings = initializeTargetSavingsData(userId);
        }
        return targetSavings;
    }

}
