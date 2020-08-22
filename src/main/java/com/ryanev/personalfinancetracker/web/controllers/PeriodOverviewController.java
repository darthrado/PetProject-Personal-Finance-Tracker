package com.ryanev.personalfinancetracker.web.controllers;

import com.ryanev.personalfinancetracker.services.dto.targets.TargetExpensesAndAmountDTO;
import com.ryanev.personalfinancetracker.services.targets.expences.TargetExpensesService;
import com.ryanev.personalfinancetracker.services.targets.savings.TargetSavingsService;
import com.ryanev.personalfinancetracker.web.dto.overview.ReportLineDTO;
import com.ryanev.personalfinancetracker.web.dto.overview.ReportLineWithTargetDTO;
import com.ryanev.personalfinancetracker.web.dto.overview.implementations.ReportLinesCreator;
import com.ryanev.personalfinancetracker.data.entities.Movement;
import com.ryanev.personalfinancetracker.data.entities.UserCacheData;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.services.DateProvider;
import com.ryanev.personalfinancetracker.services.MovementsService;
import com.ryanev.personalfinancetracker.services.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/{userId}/overview")
@Validated
public class PeriodOverviewController {

    @Autowired
    MovementsService movementsService;

    @Autowired
    UserService userService;

    @Autowired
    DateProvider dateProvider;

    @Autowired
    TargetSavingsService targetSavingsService;

    @Autowired
    TargetExpensesService targetExpensesService;

    private static final String controllerPath  = "overview";
    private static final String slash = "/";

    private List<Pair<Integer,String>> buildMonthDropdown(){
        return Arrays.stream(Month.values())
                .map(month -> Pair.of(month.getValue(),month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH)))
                .collect(Collectors.toList());
    }

    private String buildControllerBaseURL(long userId){
        return new StringBuilder(slash).append(userId).append(slash).append(controllerPath).toString();
    }

    @GetMapping
    public String showOverviewForPeriod(Model model,
                                        @PathVariable("userId") Long userId,
                                        @RequestParam(value = "month",required = false) @Min(1) @Max(12) Integer month,
                                        @RequestParam(value = "year", required = false) Integer year) throws IncorrectUserIdException {

        if (!userService.existsById(userId))
            throw new IncorrectUserIdException();

        LocalDate today = dateProvider.getNow();
        if (month == null){
            month = today.getMonthValue();
        }
        if(year == null){
            year = today.getYear();
        }
        LocalDate startDate = LocalDate.of(year,month,1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        String searchUri = buildControllerBaseURL(userId);

        List<Movement> movementList = movementsService.getMovementsForUserAndPeriod(userId, startDate,endDate);
        List<Movement> incomeList = movementList.stream().filter(f -> f.getAmount()>0).collect(Collectors.toList());
        List<Movement> expenseList = movementList.stream().filter(f -> f.getAmount()<0).collect(Collectors.toList());

        List<TargetExpensesAndAmountDTO> expenseTargets = targetExpensesService.getExpenseTargetNameAndAmount(userId,startDate);

        ReportLinesCreator reportCreator = new ReportLinesCreator();
        List<ReportLineDTO> incomeReportList = reportCreator.createReportFromListOfMovements(incomeList);
        List<ReportLineWithTargetDTO> expenseReportList =
                reportCreator.createReportFromListOfMovementsWithTargets(expenseList,expenseTargets);

        Double incomeTotal = incomeReportList.stream().mapToDouble(ReportLineDTO::getAmount).sum();
        Double expenseTotal = expenseReportList.stream().mapToDouble(ReportLineWithTargetDTO::getAmount).sum();


        List<Pair<Integer,String>> listOfMonths = buildMonthDropdown();

        UserCacheData userCache = userService.getUserCache(userId);
        List<Integer> listOfYears = buildListOfYears(today, userCache);

        Double savingsTarget = targetSavingsService.getTargetSavingsAmount(userId,startDate);

        model.addAttribute("searchUri",searchUri);
        model.addAttribute("month",Month.of(month).getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH));
        model.addAttribute("year",year);
        model.addAttribute("incomes",incomeReportList);
        model.addAttribute("expenses",expenseReportList);
        model.addAttribute("incomeTotal",incomeTotal);
        model.addAttribute("expenseTotal",expenseTotal);
        model.addAttribute("totalSaved",incomeTotal-expenseTotal);
        model.addAttribute("listOfMonths",listOfMonths);
        model.addAttribute("listOfYears",listOfYears);
        model.addAttribute("savingsTarget",savingsTarget);

        return "overview_period/overview";
    }

    private List<Integer> buildListOfYears(LocalDate today, UserCacheData userCache) {
        List<Integer> listOfYears;
        if(userCache != null && userCache.getMinMovementDate() !=null && userCache.getMaxMovementDate() != null){
            listOfYears = IntStream.range(userCache.getMinMovementDate().getYear(),userCache.getMaxMovementDate().getYear()+1)
                    .boxed().collect(Collectors.toList());
        }
        else {
            listOfYears = List.of(today.getYear());
        }
        return listOfYears;
    }


}
