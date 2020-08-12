package com.ryanev.personalfinancetracker.web.controllers;

import com.ryanev.personalfinancetracker.dto.overview.ReportLineDTO;
import com.ryanev.personalfinancetracker.dto.overview.implementations.ReportLinesCreator;
import com.ryanev.personalfinancetracker.data.entities.Movement;
import com.ryanev.personalfinancetracker.data.entities.UserCacheData;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.services.DateProvider;
import com.ryanev.personalfinancetracker.services.MovementsService;
import com.ryanev.personalfinancetracker.services.UserService;
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

        ReportLinesCreator reportCreator = new ReportLinesCreator();
        List<ReportLineDTO> incomeReportList = reportCreator.createReportFromListOfMovements(incomeList);
        List<ReportLineDTO> expenseReportList = reportCreator.createReportFromListOfMovements(expenseList);

        Double incomeTotal = incomeReportList.stream().mapToDouble(ReportLineDTO::getAmount).sum();
        Double expenseTotal = expenseReportList.stream().mapToDouble(ReportLineDTO::getAmount).sum();

        List<Pair<Integer,String>> listOfMonths = buildMonthDropdown();
        List<Integer> listOfYears;
        UserCacheData userCache = userService.getUserCache(userId);
        if(userCache != null && userCache.getMinMovementDate() !=null && userCache.getMaxMovementDate() != null){
            listOfYears = IntStream.range(userCache.getMinMovementDate().getYear(),userCache.getMaxMovementDate().getYear()+1)
                    .boxed().collect(Collectors.toList());
        }
        else {
            listOfYears = List.of(today.getYear());
        }


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

        return "overview_period/overview";
    }


}
