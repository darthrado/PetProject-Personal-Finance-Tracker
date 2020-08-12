package com.ryanev.personalfinancetracker.unit.service.target;

import static org.assertj.core.api.Assertions.*;

import com.ryanev.personalfinancetracker.data.entities.TargetExpense;
import com.ryanev.personalfinancetracker.data.repo.targets.TargetsExpensesRepository;
import com.ryanev.personalfinancetracker.exceptions.IncorrectTargetAmountException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectTargetIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.services.UserService;
import com.ryanev.personalfinancetracker.services.dto.targets.TargetExpensesAndAmountDTO;
import com.ryanev.personalfinancetracker.services.targets.core.TargetsService;
import com.ryanev.personalfinancetracker.services.targets.expences.TargetCategorySyncService;
import com.ryanev.personalfinancetracker.services.targets.expences.TargetExpensesService;
import com.ryanev.personalfinancetracker.services.targets.expences.TargetExpensesServiceImpl;
import com.ryanev.personalfinancetracker.util.target.TestTargetDetailBuilder;
import com.ryanev.personalfinancetracker.util.target.TestTargetExpensesBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;



@ExtendWith(MockitoExtension.class)
public class TargetServiceExpensesUnitTest {

    @InjectMocks
    TargetExpensesService targetExpensesService = new TargetExpensesServiceImpl();

    @Mock
    UserService userService;

    @Mock
    TargetsService targetsService;

    @Mock
    TargetsExpensesRepository targetsExpensesRepository;

    @Mock
    TargetCategorySyncService targetCategorySyncService;

    private void mockUserServiceCalls(){
        Mockito.when(userService.existsById(Mockito.anyLong())).thenReturn(true);
    }

    @Test
    public void getExpenseTargetNameAndAmount_correctCall_onlyCategoriesForTheGivenUserAreReturned() throws IncorrectUserIdException, IncorrectTargetIdException {

        //Arrange
        Long queriedUserId = 1234L;

        LocalDate queriedDate = LocalDate.of(2020,10,1);

        List<TargetExpense> targetExpensesForQueriedUser = List.of(
                TestTargetExpensesBuilder.createValidTarget().withCategoryName("Shopping").build(),
                TestTargetExpensesBuilder.createValidTarget().withCategoryName("Drinking").build(),
                TestTargetExpensesBuilder.createValidTarget().withCategoryName("Party Time").build()
        );

        mockUserServiceCalls();
        Mockito.when(targetsExpensesRepository.getAllByUserId(queriedUserId)).thenReturn(targetExpensesForQueriedUser);
        Mockito.when(targetsService.getLatestDetailForTargetAndDate(Mockito.anyLong(),Mockito.any()))
                .thenReturn(TestTargetDetailBuilder.createValidTargetDetail().build());

        //Act
        List<String> result = targetExpensesService.getExpenseTargetNameAndAmount(queriedUserId,queriedDate)
                .stream()
                .map(TargetExpensesAndAmountDTO::getCategoryName)
                .collect(Collectors.toList());

        //Assert
        assertThat(result).containsExactlyInAnyOrder("Shopping","Drinking","Party Time");

    }

    @Test
    public void getExpenseTargetNameAndAmount_correctCall_correctTargetAmountsForEach() throws IncorrectUserIdException, IncorrectTargetIdException {
        //Arrange
        Long queriedUserId = 1234L;

        LocalDate queriedDate = LocalDate.of(2020,10,1);

        Long targetOneId= 5L;
        Long targetTwoId= 10L;
        Long targetThreeId= 15L;

        Double targetOneExpectedAmount = 5000.00;
        Double targetTwoExpectedAmount = 7777.77;
        Double targetThreeExpectedAmount = 123456.78;

        List<TargetExpense> targetExpensesForQueriedUser = List.of(
                TestTargetExpensesBuilder.createValidTarget().withTargetId(targetOneId).build(),
                TestTargetExpensesBuilder.createValidTarget().withTargetId(targetTwoId).build(),
                TestTargetExpensesBuilder.createValidTarget().withTargetId(targetThreeId).build()
        );

        mockUserServiceCalls();
        Mockito.when(targetsExpensesRepository.getAllByUserId(queriedUserId)).thenReturn(targetExpensesForQueriedUser);

        Mockito.when(targetsService.getLatestDetailForTargetAndDate(targetOneId,queriedDate))
                .thenReturn(TestTargetDetailBuilder.createValidTargetDetail().withAmount(targetOneExpectedAmount).build());
        Mockito.when(targetsService.getLatestDetailForTargetAndDate(targetTwoId,queriedDate))
                .thenReturn(TestTargetDetailBuilder.createValidTargetDetail().withAmount(targetTwoExpectedAmount).build());
        Mockito.when(targetsService.getLatestDetailForTargetAndDate(targetThreeId,queriedDate))
                .thenReturn(TestTargetDetailBuilder.createValidTargetDetail().withAmount(targetThreeExpectedAmount).build());

        //Act
        List<TargetExpensesAndAmountDTO> result = targetExpensesService.getExpenseTargetNameAndAmount(queriedUserId,queriedDate);

        //Assert
        assertThat(result)
                .anySatisfy(element -> {
                    assertThat(element.getTargetId()).isEqualTo(targetOneId);
                    assertThat(element.getAmount()).isEqualTo(targetOneExpectedAmount);
                })
                .anySatisfy(element -> {
                    assertThat(element.getTargetId()).isEqualTo(targetTwoId);
                    assertThat(element.getAmount()).isEqualTo(targetTwoExpectedAmount);
                })
                .anySatisfy(element -> {
                    assertThat(element.getTargetId()).isEqualTo(targetThreeId);
                    assertThat(element.getAmount()).isEqualTo(targetThreeExpectedAmount);
                });

    }

    @Test
    public void getExpenseTargetNameAndAmount_incorrectCall_IncorrectUserIdExceptionIsThrown(){

        //Arrange
        Long incorrectUserId = 23560L;
        LocalDate queriedDate = LocalDate.of(2020,10,1);

        Mockito.when(userService.existsById(incorrectUserId)).thenReturn(false);

        //Act + Assert
        assertThatExceptionOfType(IncorrectUserIdException.class)
                .isThrownBy(() -> targetExpensesService.getExpenseTargetNameAndAmount(incorrectUserId,queriedDate) );
    }

    @Test
    public void saveExpensesTarget_correctCall_targetDetailRepoIsSuccessfullyCalled() throws IncorrectTargetIdException, IncorrectTargetAmountException {

        //Arrange
        Long targetExpenseId = 3958L;
        Double amountToSave = 3333.33;

        Mockito.when(targetsExpensesRepository.existsById(targetExpenseId)).thenReturn(true);

        //Act
        targetExpensesService.saveExpensesTargetAmount(targetExpenseId,amountToSave);
        //Assert
        Mockito.verify(targetsService).saveTargetAmount(targetExpenseId, amountToSave);

    }

    @Test
    public void saveExpensesTarget_targetIdNotPresentInExpenseTargetsRepo_IncorrectTargetIdExceptionIsThrown(){

        //Arrange
        Long incorrectTargetExpenseId = 3958L;
        Double amountToSave = 3333.33;

        Mockito.when(targetsExpensesRepository.existsById(incorrectTargetExpenseId)).thenReturn(false);

        //Act + Assert
        assertThatExceptionOfType(IncorrectTargetIdException.class)
                .isThrownBy(() -> targetExpensesService.saveExpensesTargetAmount(incorrectTargetExpenseId,amountToSave));
    }

    @Test
    public void saveExpensesTarget_amountPassedIsNegative_IncorrectAmountExceptionIsThrown(){
        //Arrange
        Long targetExpenseId = 3958L;
        Double negativeAmountToSave = -3333.33;

        Mockito.when(targetsExpensesRepository.existsById(targetExpenseId)).thenReturn(true);

        //Act + Assert
        assertThatExceptionOfType(IncorrectTargetAmountException.class)
                .isThrownBy(() -> targetExpensesService.saveExpensesTargetAmount(targetExpenseId,negativeAmountToSave));
    }

}
