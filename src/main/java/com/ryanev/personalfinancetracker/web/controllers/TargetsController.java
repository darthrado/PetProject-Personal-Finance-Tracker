package com.ryanev.personalfinancetracker.web.controllers;


import com.ryanev.personalfinancetracker.dto.targets.TargetExpensesDTO;
import com.ryanev.personalfinancetracker.dto.targets.TargetSavingsDTO;
import com.ryanev.personalfinancetracker.exceptions.IncorrectTargetAmountException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectTargetIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.exceptions.TargetDataInvalidException;
import com.ryanev.personalfinancetracker.services.DateProvider;
import com.ryanev.personalfinancetracker.services.dto.targets.TargetExpensesAndAmountDTO;
import com.ryanev.personalfinancetracker.services.targets.expences.TargetExpensesService;
import com.ryanev.personalfinancetracker.services.targets.savings.TargetSavingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.persistence.MapsId;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/{userId}/targets")
public class TargetsController {


    private static final String controllerPath  = "targets";
    private static final String slash = "/";


    private String buildControllerBaseURL(long userId){
        return new StringBuilder(slash).append(userId).append(slash).append(controllerPath).toString();
    }


    @Autowired
    DateProvider dateProvider;

    @Autowired
    TargetSavingsService targetSavingsService;

    @Autowired
    TargetExpensesService targetExpensesService;

    @GetMapping
    public String showTargetsPage(Model model,
                                  @PathVariable("userId") Long userId) throws IncorrectUserIdException, TargetDataInvalidException, IncorrectTargetIdException {

        LocalDate date = dateProvider.getNow();


        String savingsTargetUpdateURL = buildControllerBaseURL(userId).concat(slash).concat("update_savings");
        String expensesTargetUpdateURL = buildControllerBaseURL(userId).concat(slash).concat("update_expenses");

        List<TargetExpensesAndAmountDTO> expenseTargets = targetExpensesService
                .getExpenseTargetNameAndAmount(userId,date);


        TargetSavingsDTO targetSavingsDTO = new TargetSavingsDTO();
        targetSavingsDTO.setAmount(targetSavingsService.getTargetSavingsAmount(userId,date));



        TargetExpensesDTO expenseTargetsDTO = new TargetExpensesDTO();
        expenseTargetsDTO.setTargets(expenseTargets);


        model.addAttribute("expenseTargetsForm",expenseTargetsDTO);
        model.addAttribute("savingsTarget",targetSavingsDTO);
        model.addAttribute("expensesTargetUpdateURL",expensesTargetUpdateURL);
        model.addAttribute("savingsTargetUpdateURL",savingsTargetUpdateURL);


        return "targets/targets-page";
    }


    @PostMapping("/update_savings")
    public String updateSavings(Model model,
                                TargetSavingsDTO savingsTarget,
                                @PathVariable("userId") Long userId) throws IncorrectUserIdException, IncorrectTargetAmountException {

        targetSavingsService.saveSavingsTargetForUser(userId,savingsTarget.getAmount());

        return "redirect:"+buildControllerBaseURL(userId);

    }


    private Map<Long,Double> transformDtoToMapOfAmounts(List<TargetExpensesAndAmountDTO> targetExpensesAndAmountDTO){

        return targetExpensesAndAmountDTO
                .stream()
                .collect(HashMap::new, (m, v)->m.put(v.getTargetId(), v.getAmount()), HashMap::putAll);
    }

    @PostMapping("/update_expenses")
    public String updateExpenses(Model model,
                                 TargetExpensesDTO targetExpensesDTO,
                                @PathVariable("userId") Long userId) throws IncorrectUserIdException, IncorrectTargetIdException, IncorrectTargetAmountException {

        LocalDate date = dateProvider.getNow();
        Map<Long,Double> newExpenseTargets = transformDtoToMapOfAmounts(targetExpensesDTO.getTargets());

        Map<Long,Double> currentExpenseTargets = transformDtoToMapOfAmounts(targetExpensesService.getExpenseTargetNameAndAmount(userId,date));

        Map<Long, Double> modifiedEntries = extractModifiedEntriesOnly(newExpenseTargets, currentExpenseTargets);

        for (Map.Entry<Long,Double> modifiedEntry:modifiedEntries.entrySet()) {
            targetExpensesService.saveExpensesTargetAmount(modifiedEntry.getKey(),modifiedEntry.getValue());
        }

        return "redirect:"+buildControllerBaseURL(userId);

    }

    private Map<Long, Double> extractModifiedEntriesOnly(Map<Long, Double> newExpenseTargets, Map<Long, Double> currentExpenseTargets) {
        return newExpenseTargets
                    .entrySet()
                    .stream()
                    .filter(expenseTarget -> !expenseTarget.getValue().equals(currentExpenseTargets.get(expenseTarget.getKey())))
                    .collect(HashMap::new, (m, v)->m.put(v.getKey(), v.getValue()), HashMap::putAll);
    }

}
