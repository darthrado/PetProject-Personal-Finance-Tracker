package com.ryanev.personalfinancetracker.unit.service.target;

import static org.assertj.core.api.Assertions.*;

import com.ryanev.personalfinancetracker.data.entities.Target;
import com.ryanev.personalfinancetracker.data.repo.targets.TargetsSavingsRepository;
import com.ryanev.personalfinancetracker.exceptions.IncorrectTargetAmountException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectTargetIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.services.users.UserService;
import com.ryanev.personalfinancetracker.services.targets.core.TargetsService;
import com.ryanev.personalfinancetracker.services.targets.savings.TargetSavingsService;
import com.ryanev.personalfinancetracker.services.targets.savings.TargetSavingsServiceImpl;
import com.ryanev.personalfinancetracker.util.target.TestTargetBuilder;
import com.ryanev.personalfinancetracker.util.target.TestTargetDetailBuilder;
import com.ryanev.personalfinancetracker.util.target.TestTargetSavingsBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TargetSavingsServiceUnitTest {

    @InjectMocks
    TargetSavingsService targetSavingsService = new TargetSavingsServiceImpl();

    @Mock
    UserService userService;

    @Mock
    TargetsService targetsService;

    @Mock
    TargetsSavingsRepository targetsSavingsRepository;

    @Test
    public void getTargetSavingsAmount_correctCall_returnsTheExpectedAmount() throws IncorrectUserIdException, IncorrectTargetIdException {

        //Arrange
        Long userId = 12355L;
        LocalDate queryDate = LocalDate.of(2020,1,1);

        Double expectedAmount = 7483.22;

        Long internalIdForTarget = 23451L;

        Mockito.when(userService.existsById(userId)).thenReturn(true);

        Mockito.when(targetsSavingsRepository.getByUserId(userId))
                .thenReturn(Optional.of(TestTargetSavingsBuilder.createValidTarget().withTargetId(internalIdForTarget).build()));
        Mockito.when(targetsService.getLatestDetailForTargetAndDate(internalIdForTarget,queryDate))
                .thenReturn(TestTargetDetailBuilder.createValidTargetDetail().withAmount(expectedAmount).build());

        //Act
        Double result = targetSavingsService.getTargetSavingsAmount(userId,queryDate);

        //Assert
        assertThat(result).isEqualTo(expectedAmount);

    }

    @Test
    public void getTargetSavingsAmount_incorrectCall_IncorrectUserIdExceptionIsThrown(){
        //Arrange
        Long incorrectUserId = 12355L;
        LocalDate queryDate = LocalDate.of(2020,1,1);

        Mockito.when(userService.existsById(incorrectUserId)).thenReturn(false);

        //Act + Assert
        assertThatExceptionOfType(IncorrectUserIdException.class)
                .isThrownBy(() -> targetSavingsService.getTargetSavingsAmount(incorrectUserId,queryDate));

    }

    @Test
    public void saveSavingsTargetForUser_correctCall_targetDetailSaveIsCalled() throws IncorrectUserIdException, IncorrectTargetIdException, IncorrectTargetAmountException {

        //Arrange
        Long userId = 3958L;
        Double amountToSave = 3333.33;

        Long internalSavingTargetId = 2345L;

        Mockito.when(userService.existsById(userId)).thenReturn(true);
        Mockito.when(targetsSavingsRepository.getByUserId(userId))
                .thenReturn(Optional.of(TestTargetSavingsBuilder.createValidTarget().withTargetId(internalSavingTargetId).build()));

        //Act
        targetSavingsService.saveSavingsTargetForUser(userId,amountToSave);
        //Assert
        Mockito.verify(targetsService).saveTargetAmount(internalSavingTargetId, amountToSave);

    }

    @Test
    public void saveSavingsTargetForUser_correctCall_ifSavingsTargetNotPresentInRepoCreateOne() throws IncorrectUserIdException, IncorrectTargetAmountException {
        //Arrange
        Long userId = 3958L;
        Double amountToSave = 3333.33;

        Target newlyCreatedTarget = TestTargetBuilder.createValidTarget().build();

        Mockito.when(userService.existsById(userId)).thenReturn(true);
        Mockito.when(targetsService.createNewTargetForUser(userId)).thenReturn(newlyCreatedTarget);
        Mockito.when(targetsSavingsRepository.getByUserId(userId))
                .thenReturn(Optional.empty());

        //Act
        targetSavingsService.saveSavingsTargetForUser(userId,amountToSave);
        //Assert
        Mockito.verify(targetsService).createNewTargetForUser(userId);
        Mockito.verify(targetsSavingsRepository).save(Mockito.argThat(targetSavings -> targetSavings.getTarget().equals(newlyCreatedTarget)));
    }

    @Test
    public void saveSavingsTargetForUser_incorrectCall_userIdNotExists_IncorrectUserIdExceptionIsThrown() {
        //Arrange
        Long userId = 3958L;

        Mockito.when(userService.existsById(userId)).thenReturn(false);

        //Act+Assert
        assertThatExceptionOfType(IncorrectUserIdException.class)
                .isThrownBy(()-> targetSavingsService.saveSavingsTargetForUser(userId, Mockito.anyDouble()));
    }

    @Test
    public void saveSavingsTargetForUser_incorrectCall_amountIsNegative_IncorrectUserIdExceptionIsThrown() {
        //Arrange
        Long userId = Mockito.anyLong();
        Double negativeAmount = -344663.00;

        Mockito.when(userService.existsById(userId)).thenReturn(true);

        //Act+Assert
        assertThatExceptionOfType(IncorrectTargetAmountException.class)
                .isThrownBy(()-> targetSavingsService.saveSavingsTargetForUser(userId, negativeAmount));
    }

}
