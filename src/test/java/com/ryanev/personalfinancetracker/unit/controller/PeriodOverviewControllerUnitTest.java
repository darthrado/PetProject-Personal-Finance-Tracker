package com.ryanev.personalfinancetracker.unit.controller;


import com.ryanev.personalfinancetracker.controllers.PeriodOverviewController;
import com.ryanev.personalfinancetracker.entities.Movement;
import com.ryanev.personalfinancetracker.entities.MovementCategory;
import com.ryanev.personalfinancetracker.entities.UserCacheData;
import com.ryanev.personalfinancetracker.services.CategoriesService;
import com.ryanev.personalfinancetracker.services.DateProvider;
import com.ryanev.personalfinancetracker.services.MovementsService;
import com.ryanev.personalfinancetracker.services.UserService;
import com.ryanev.personalfinancetracker.util.TestCategoryBuilder;
import com.ryanev.personalfinancetracker.util.TestMovementBuilder;
import com.ryanev.personalfinancetracker.util.TestUserBuilder;
import com.ryanev.personalfinancetracker.util.TestUserCacheBuilder;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.List;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PeriodOverviewController.class)
public class PeriodOverviewControllerUnitTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private CategoriesService categoriesService;
    @MockBean
    private MovementsService movementsService;
    @MockBean
    private DateProvider dateProvider;

    private String getControllerBaseURL(Long userId){
        return "/"+userId+"/overview";
    }
    private Long someUserId(){
        return 888777666L;
    }

    @BeforeEach
    private void mockTime(){
        Mockito.when(dateProvider.getNow()).thenReturn(LocalDate.now());
    }

    @Test
    public void overviewPage_correctGetRequest_negativeMovementsAreGroupedByCategoryAsExpenses() throws Exception {

        Long userId = someUserId();

        Mockito.when(userService.existsById(userId)).thenReturn(true);

        MovementCategory salaryCategory = TestCategoryBuilder.createValidCategory().withName("Salary").build();
        MovementCategory shoppingCategory = TestCategoryBuilder.createValidCategory().withName("Shopping").build();
        MovementCategory takeAwayCategory = TestCategoryBuilder.createValidCategory().withName("Take Away").build();

        List<Movement> listOfAllMovements = List.of(
                TestMovementBuilder.createValidMovement()
                        .withCategory(salaryCategory)
                        .withAmount(8000.00).build(),
                TestMovementBuilder.createValidMovement()
                        .withCategory(shoppingCategory)
                        .withAmount(-200.00).build(),
                TestMovementBuilder.createValidMovement()
                        .withCategory(shoppingCategory)
                        .withAmount(-300.00).build(),
                TestMovementBuilder.createValidMovement()
                        .withCategory(takeAwayCategory)
                        .withAmount(-300.00).build(),
                TestMovementBuilder.createValidMovement()
                        .withCategory(takeAwayCategory)
                        .withAmount(300.00).build()
        );

        Integer expectedExpenseCategoriesOnScreen = 2;

        Mockito.when(movementsService.getMovementsForUserAndPeriod(Mockito.eq(userId),Mockito.any(),Mockito.any()))
                .thenReturn(listOfAllMovements);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("expenses", Matchers.iterableWithSize(expectedExpenseCategoriesOnScreen)))
                .andExpect(MockMvcResultMatchers.model()
                        .attribute("expenses", Matchers.hasItem(Matchers.allOf(
                                Matchers.hasProperty("categoryName",Matchers.is("Shopping")),
                                Matchers.hasProperty("amount",Matchers.is(Double.valueOf(500)))))))
                .andExpect(MockMvcResultMatchers.model()
                        .attribute("expenses", Matchers.hasItem(Matchers.allOf(
                                Matchers.hasProperty("categoryName",Matchers.is("Take Away")),
                                Matchers.hasProperty("amount",Matchers.is(Double.valueOf(300)))))))
        ;

    }
    @Test
    public void overviewPage_correctGetRequest_positiveMovementsAreGroupedByCategoryAsIncome() throws Exception {
        Long userId = someUserId();

        MovementCategory salaryCategory = TestCategoryBuilder.createValidCategory().withName("Salary").build();
        MovementCategory freelanceCategory = TestCategoryBuilder.createValidCategory().withName("Freelance").build();
        MovementCategory takeAwayCategory = TestCategoryBuilder.createValidCategory().withName("Take Away").build();

        Mockito.when(userService.existsById(userId)).thenReturn(true);

        List<Movement> listOfAllMovements = List.of(
                TestMovementBuilder.createValidMovement()
                        .withCategory(salaryCategory)
                        .withAmount(8000.00).build(),
                TestMovementBuilder.createValidMovement()
                        .withCategory(freelanceCategory)
                        .withAmount(200.00).build(),
                TestMovementBuilder.createValidMovement()
                        .withCategory(freelanceCategory)
                        .withAmount(300.00).build(),
                TestMovementBuilder.createValidMovement()
                        .withCategory(takeAwayCategory)
                        .withAmount(-300.00).build(),
                TestMovementBuilder.createValidMovement()
                        .withCategory(takeAwayCategory)
                        .withAmount(300.00).build()
        );

        Integer expectedExpenseCategoriesOnScreen = 3;

        Mockito.when(movementsService.getMovementsForUserAndPeriod(Mockito.eq(userId),Mockito.any(),Mockito.any()))
                .thenReturn(listOfAllMovements);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("incomes", Matchers.iterableWithSize(expectedExpenseCategoriesOnScreen)))
                .andExpect(MockMvcResultMatchers.model()
                        .attribute("incomes", Matchers.hasItem(Matchers.allOf(
                                Matchers.hasProperty("categoryName",Matchers.is("Salary")),
                                Matchers.hasProperty("amount",Matchers.is(Double.valueOf(8000)))))))
                .andExpect(MockMvcResultMatchers.model()
                        .attribute("incomes", Matchers.hasItem(Matchers.allOf(
                                Matchers.hasProperty("categoryName",Matchers.is("Freelance")),
                                Matchers.hasProperty("amount",Matchers.is(Double.valueOf(500)))))))
                .andExpect(MockMvcResultMatchers.model()
                        .attribute("incomes", Matchers.hasItem(Matchers.allOf(
                                Matchers.hasProperty("categoryName",Matchers.is("Take Away")),
                                Matchers.hasProperty("amount",Matchers.is(Double.valueOf(300)))))))
        ;
    }
    @Test
    public void overviewPage_correctGetRequest_totalForExpensesOfTheMonthIsShown() throws Exception {
        Long userId = someUserId();

        Mockito.when(userService.existsById(userId)).thenReturn(true);

        MovementCategory salaryCategory = TestCategoryBuilder.createValidCategory().withName("Salary").build();
        MovementCategory shoppingCategory = TestCategoryBuilder.createValidCategory().withName("Shopping").build();
        MovementCategory takeAwayCategory = TestCategoryBuilder.createValidCategory().withName("Take Away").build();

        List<Movement> listOfAllMovements = List.of(
                TestMovementBuilder.createValidMovement()
                        .withCategory(salaryCategory)
                        .withAmount(8000.00).build(),
                TestMovementBuilder.createValidMovement()
                        .withCategory(shoppingCategory)
                        .withAmount(-200.00).build(),
                TestMovementBuilder.createValidMovement()
                        .withCategory(shoppingCategory)
                        .withAmount(-300.00).build(),
                TestMovementBuilder.createValidMovement()
                        .withCategory(takeAwayCategory)
                        .withAmount(-300.00).build(),
                TestMovementBuilder.createValidMovement()
                        .withCategory(takeAwayCategory)
                        .withAmount(300.00).build()
        );

        Double expectedIncomeTotal = 800.00;

        Mockito.when(movementsService.getMovementsForUserAndPeriod(Mockito.eq(userId),Mockito.any(),Mockito.any()))
                .thenReturn(listOfAllMovements);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("expenseTotal", Matchers.is(expectedIncomeTotal)));

    }
    @Test
    public void overviewPage_correctGetRequest_totalForIncomeOfTheMonthIsShown() throws Exception {
        Long userId = someUserId();

        Mockito.when(userService.existsById(userId)).thenReturn(true);

        MovementCategory salaryCategory = TestCategoryBuilder.createValidCategory().withName("Salary").build();
        MovementCategory freelanceCategory = TestCategoryBuilder.createValidCategory().withName("Freelance").build();
        MovementCategory takeAwayCategory = TestCategoryBuilder.createValidCategory().withName("Take Away").build();

        List<Movement> listOfAllMovements = List.of(
                TestMovementBuilder.createValidMovement()
                        .withCategory(salaryCategory)
                        .withAmount(8000.00).build(),
                TestMovementBuilder.createValidMovement()
                        .withCategory(freelanceCategory)
                        .withAmount(200.00).build(),
                TestMovementBuilder.createValidMovement()
                        .withCategory(freelanceCategory)
                        .withAmount(300.00).build(),
                TestMovementBuilder.createValidMovement()
                        .withCategory(takeAwayCategory)
                        .withAmount(-300.00).build(),
                TestMovementBuilder.createValidMovement()
                        .withCategory(takeAwayCategory)
                        .withAmount(300.00).build()
        );

        Double expectedExpensesTotal = 8800.00;

        Mockito.when(movementsService.getMovementsForUserAndPeriod(Mockito.eq(userId),Mockito.any(),Mockito.any()))
                .thenReturn(listOfAllMovements);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("incomeTotal", Matchers.is(expectedExpensesTotal)));


    }
    @Test
    public void overviewPage_correctGetRequest_differenceBetweenIncomeAndExpenseIsShown() throws Exception {
        Long userId = someUserId();

        Mockito.when(userService.existsById(userId)).thenReturn(true);

        MovementCategory salaryCategory = TestCategoryBuilder.createValidCategory().withName("Salary").build();
        MovementCategory freelanceCategory = TestCategoryBuilder.createValidCategory().withName("Freelance").build();
        MovementCategory takeAwayCategory = TestCategoryBuilder.createValidCategory().withName("Take Away").build();

        List<Movement> listOfAllMovements = List.of(
                TestMovementBuilder.createValidMovement()
                        .withCategory(salaryCategory)
                        .withAmount(8000.00).build(),
                TestMovementBuilder.createValidMovement()
                        .withCategory(freelanceCategory)
                        .withAmount(200.00).build(),
                TestMovementBuilder.createValidMovement()
                        .withCategory(freelanceCategory)
                        .withAmount(300.00).build(),
                TestMovementBuilder.createValidMovement()
                        .withCategory(takeAwayCategory)
                        .withAmount(-300.00).build(),
                TestMovementBuilder.createValidMovement()
                        .withCategory(takeAwayCategory)
                        .withAmount(-700.00).build()
        );

        Double expectedTotalSaved = 7500.00;

        Mockito.when(movementsService.getMovementsForUserAndPeriod(Mockito.eq(userId),Mockito.any(),Mockito.any()))
                .thenReturn(listOfAllMovements);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("totalSaved", Matchers.is(expectedTotalSaved)));


    }


    @ParameterizedTest
    @CsvSource({"1,January",
            "2,February",
            "3,March",
            "4,April",
            "5,May",
            "6,June",
            "7,July",
            "8,August",
            "9,September",
            "10,October",
            "11,November",
            "12,December"})
    public void overviewPage_correctGetRequest_monthDropdownContainsAllMonths(Integer monthNumber,
                                                                              String monthName) throws Exception {
        Long userId = someUserId();

        Mockito.when(userService.existsById(userId)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model()
                        .attribute("listOfMonths", Matchers.hasItem(Matchers.allOf(
                                Matchers.hasProperty("first",Matchers.is(monthNumber)),
                                Matchers.hasProperty("second",Matchers.is(monthName))))));

    }
    @Test
    public void overviewPage_correctGetRequest_yearDropdownHasOnlyYearsInMovementRange() throws Exception {
        Long userId = someUserId();

        UserCacheData userCacheData = TestUserCacheBuilder.createValidUser()
                .withUser(TestUserBuilder.createValidUser().withId(userId).build())
                .withMinMovementDate(LocalDate.of(2017,10,11))
                .withMaxMovementDate(LocalDate.of(2021,12,31))
                .build();

        Mockito.when(userService.existsById(userId)).thenReturn(true);
        Mockito.when(userService.getUserCache(userId)).thenReturn(userCacheData);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model()
                        .attribute("listOfYears",Matchers.hasItems(2017,2018,2019,2020,2021)));

    }

    @ParameterizedTest
    @CsvSource({"1,January",
            "2,February",
            "3,March",
            "4,April",
            "5,May",
            "6,June",
            "7,July",
            "8,August",
            "9,September",
            "10,October",
            "11,November",
            "12,December"})
    public void overviewPage_correctGetRequest_theSelectedPeriodIsProperlyVisualized(Integer monthNumber,
                                                                                     String monthName) throws Exception {
        Long userId = someUserId();
        Integer year = 2028;

        Mockito.when(userService.existsById(userId)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId))
                .param("month",monthNumber.toString())
                .param("year",year.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("month",Matchers.is(monthName)))
                .andExpect(MockMvcResultMatchers.model().attribute("year",Matchers.is(year)));
    }

    @ParameterizedTest
    @CsvSource({"2028,10,1,31","2020,2,1,29","2019,2,1,28","2020,9,1,30"})
    public void overviewPage_correctGetRequest_movementsOnlyForThePeriodSelectedAreTaken(Integer year,
                                                                                         Integer month,
                                                                                         Integer firstDayOfMonth,
                                                                                         Integer lastDayOfMonth) throws Exception {


        Long userId = someUserId();
        LocalDate startDate = LocalDate.of(year,month,firstDayOfMonth);
        LocalDate endDate = LocalDate.of(year,month,lastDayOfMonth);

        Mockito.when(userService.existsById(userId)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId))
                .param("month",month.toString())
                .param("year",year.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(movementsService).getMovementsForUserAndPeriod(userId,startDate,endDate);

    }
    @Test
    public void overviewPage_correctGetRequest_whenNoMonthAndDateAreGivenUseCurrentDateAndMonth() throws Exception {
        Long userId = someUserId();

        Integer month = 10;
        Integer year = 2019;
        String monthName = "October";

        Mockito.when(userService.existsById(userId)).thenReturn(true);
        Mockito.when(dateProvider.getNow()).thenReturn(LocalDate.of(year,month,30));

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("month",Matchers.is(monthName)))
                .andExpect(MockMvcResultMatchers.model().attribute("year",Matchers.is(year)));
    }

    @Test
    public void overviewPage_incorrectGetRequest_userIdInvalid_returnsBadRequest() throws Exception {
        Long userId = someUserId();

        Mockito.when(userService.existsById(userId)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }
//    @ParameterizedTest
//    @ValueSource(ints = {-1,0,13,20,100})
//    public void overviewPage_incorrectGetRequest_userIdMonth_returnsBadRequest(Integer month) throws Exception {
//        Long userId = someUserId();
//
//        Integer year = 2020;
//
//        Mockito.when(userService.existsById(userId)).thenReturn(true);
//
//        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId))
//                .param("month",month.toString())
//                .param("year",year.toString()))
//                .andExpect(MockMvcResultMatchers.status().is5xxServerError());
//
//        //TODO rework this to throw bad request
//    }
}
