package com.ryanev.personalfinancetracker.services.targets.core;

import com.ryanev.personalfinancetracker.data.entities.*;
import com.ryanev.personalfinancetracker.data.repo.targets.TargetDetailsRepository;
import com.ryanev.personalfinancetracker.data.repo.targets.TargetsRepository;
import com.ryanev.personalfinancetracker.exceptions.IncorrectTargetAmountException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectTargetIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.services.*;
import com.ryanev.personalfinancetracker.services.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;


@Service
public class DefaultTargetsService implements TargetsService {

    @Autowired
    UserService userService;

    @Autowired
    TargetsRepository targetsRepository;

    @Autowired
    TargetDetailsRepository targetDetailsRepository;

    @Autowired
    DateProvider dateProvider;

    @Override
    public void saveTargetAmount(Long targetId, Double amount) throws IncorrectTargetIdException, IncorrectTargetAmountException {
        Target target;
        try{
            target = targetsRepository.findById(targetId).orElseThrow();
        }catch (NoSuchElementException e){
            throw new IncorrectTargetIdException();
        }

        saveTargetAmount(target,amount);
    }

    private void saveTargetAmount(Target target, Double amount) throws IncorrectTargetIdException, IncorrectTargetAmountException {

        if(amount!=null&&amount<0){
            throw new IncorrectTargetAmountException("Amount can't be negative");
        }

        if(target.getId()==null){
            throw new IncorrectTargetIdException();
        }

        LocalDate saveValueDate = dateProvider.getNow().with(TemporalAdjusters.firstDayOfMonth());

        TargetDetail detail;

        try {
            detail = getLatestDetailForTargetAndDate(target.getId(),saveValueDate);
        }catch (IncorrectTargetIdException e){
            detail = null;
        }

        if(detail==null||detail.getValueDate().isBefore(saveValueDate)){
            detail = buildEmptyTargetDetailForTarget(target);
            detail.setValueDate(saveValueDate);
        }

        detail.setAmount(amount);


        targetDetailsRepository.save(detail);
    }


    private TargetDetail buildEmptyTargetDetailForTarget(Target target){
        TargetDetail result = new TargetDetail();
        result.setTarget(target);
        result.setValueDate(dateProvider.getNow().with(TemporalAdjusters.firstDayOfMonth()));

        return result;
    }

    @Override
    public TargetDetail getLatestDetailForTargetAndDate(Long targetId, LocalDate date) throws IncorrectTargetIdException {
        try {
            return targetDetailsRepository.findAllByTargetId(targetId)
                    .stream()
                    .filter(detail -> (date==null||detail.getValueDate().isBefore(date)||detail.getValueDate().isEqual(date)))
                    .max(Comparator.comparing(TargetDetail::getValueDate))
                    .orElseThrow();
        }catch (NoSuchElementException e){
            throw new IncorrectTargetIdException();
        }

    }

    @Override
    public void deleteTargetsByIds(List<Long> listOfTargetIds) {
        targetDetailsRepository.deleteAllByTargetIdIn(listOfTargetIds);
        targetsRepository.deleteAllByIdIn(listOfTargetIds);
    }

    @Override
    public Target createNewTargetForUser(Long userId) throws IncorrectUserIdException {

        if (!userService.existsById(userId)){
            throw new IncorrectUserIdException();
        }

        Target newTarget = new Target();
        newTarget.setUser(userService.getUserById(userId));
        newTarget = targetsRepository.save(newTarget);

        try {
            saveTargetAmount(newTarget,null);
        } catch (IncorrectTargetIdException | IncorrectTargetAmountException e) {
            throw new RuntimeException("This shouldn't happen. Expect valid target in the DB but didn't get one");
        }

        return newTarget;
    }
}
