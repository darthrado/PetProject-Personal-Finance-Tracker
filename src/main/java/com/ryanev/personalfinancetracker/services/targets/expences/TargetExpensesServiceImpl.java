package com.ryanev.personalfinancetracker.services.targets.expences;

import com.ryanev.personalfinancetracker.data.repo.targets.TargetsExpensesRepository;
import com.ryanev.personalfinancetracker.exceptions.IncorrectTargetAmountException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectTargetIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.services.UserService;
import com.ryanev.personalfinancetracker.services.categories.CategoryChangeNotifier;
import com.ryanev.personalfinancetracker.services.dto.targets.TargetExpensesAndAmountDTO;
import com.ryanev.personalfinancetracker.services.targets.core.TargetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TargetExpensesServiceImpl implements TargetExpensesService {

    UserService userService;

    TargetsService targetsService;

    TargetsExpensesRepository targetsExpensesRepository;

    CategoryChangeNotifier categoryChangeNotifier;

    TargetCategoryObserver targetCategoryObserver;

    @Autowired
    public TargetExpensesServiceImpl(UserService userService,
                                     TargetsService targetsService,
                                     TargetsExpensesRepository targetsExpensesRepository,
                                     CategoryChangeNotifier categoryChangeNotifier,
                                     TargetCategoryObserver targetCategoryObserver) {
        this.userService = userService;
        this.targetsService = targetsService;
        this.targetsExpensesRepository = targetsExpensesRepository;
        this.categoryChangeNotifier = categoryChangeNotifier;
        this.targetCategoryObserver = targetCategoryObserver;

        this.categoryChangeNotifier.addObserver(targetCategoryObserver);
    }

    @Override
    public List<TargetExpensesAndAmountDTO> getExpenseTargetNameAndAmount(Long userId, LocalDate date) throws IncorrectUserIdException {
        if (!userService.existsById(userId)){
            throw new IncorrectUserIdException();
        }

        List<TargetExpensesAndAmountDTO> targetActiveExpensesDTO = targetsExpensesRepository.getAllByUserId(userId)
                .stream()
                .filter(targetExpense -> targetExpense.getCategory().getFlagActive())
                .map(targetExpense ->  new TargetExpensesAndAmountDTO(targetExpense.getTargetId(),targetExpense.getCategory().getName(),null))
                .collect(Collectors.toList());

        for (TargetExpensesAndAmountDTO target:targetActiveExpensesDTO) {
            try {
                target.setAmount(targetsService.getLatestDetailForTargetAndDate(target.getTargetId(), date).getAmount());
            }
            catch(IncorrectTargetIdException e){
                target.setAmount(0.0);
                //no target detail for that period so amount is 0
                //TODO handle this with proper exception IncorrectTargetIdException is not the correct exception
            }
        }

        return targetActiveExpensesDTO;
    }

    @Override
    public void saveExpensesTargetAmount(Long targetId,Double amount) throws IncorrectTargetIdException, IncorrectTargetAmountException {

        if(!targetsExpensesRepository.existsById(targetId)){
            throw new IncorrectTargetIdException();
        }
        if (amount!=null&&amount<0){
            throw new IncorrectTargetAmountException("Amount can't be negative");
        }

        targetsService.saveTargetAmount(targetId,amount);
    }




}
