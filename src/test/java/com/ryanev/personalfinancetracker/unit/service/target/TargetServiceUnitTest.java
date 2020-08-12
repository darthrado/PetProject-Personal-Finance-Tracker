package com.ryanev.personalfinancetracker.unit.service.target;

import static org.assertj.core.api.Assertions.*;

import com.ryanev.personalfinancetracker.data.entities.Target;
import com.ryanev.personalfinancetracker.data.entities.TargetDetail;
import com.ryanev.personalfinancetracker.data.entities.User;
import com.ryanev.personalfinancetracker.data.repo.targets.TargetDetailsRepository;
import com.ryanev.personalfinancetracker.data.repo.targets.TargetsRepository;
import com.ryanev.personalfinancetracker.exceptions.IncorrectTargetAmountException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectTargetIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.services.DateProvider;
import com.ryanev.personalfinancetracker.services.UserService;
import com.ryanev.personalfinancetracker.services.targets.core.DefaultTargetsService;
import com.ryanev.personalfinancetracker.services.targets.core.TargetsService;
import com.ryanev.personalfinancetracker.util.target.TestTargetBuilder;
import com.ryanev.personalfinancetracker.util.target.TestTargetDetailBuilder;
import com.ryanev.personalfinancetracker.util.user.TestUserBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TargetServiceUnitTest {

    @Mock
    UserService userService;

    @Mock
    TargetsRepository targetsRepository;

    @Mock
    TargetDetailsRepository targetDetailsRepository;

    @Mock
    DateProvider dateProvider;

    @InjectMocks
    TargetsService targetsService = new DefaultTargetsService();


    private void mockUserServiceForValidUser(User user){
        if(user.getId()==null){
           throw new RuntimeException("User ID must not be null");
        }

        Mockito.when(userService.existsById(user.getId())).thenReturn(true);
        Mockito.lenient().when(userService.getUserById(user.getId())).thenReturn(user);
    }

    private void mockUserServiceForValidUserWithId(Long userId){
        mockUserServiceForValidUser(TestUserBuilder.createValidUser().withId(userId).build());
    }


    @Test
    public void getDetailForTargetAndDate_correctCall_latestDetailForTheGivenDateIsReturned() throws IncorrectTargetIdException {

        //Arrange
        Long targetId = 33344L;
        LocalDate date = LocalDate.of(2020,7,11);

        TargetDetail detailOne = TestTargetDetailBuilder.createValidTargetDetail().withValueDate(LocalDate.of(2020,8,11)).withId(targetId).build();
        TargetDetail detailTwo = TestTargetDetailBuilder.createValidTargetDetail().withValueDate(LocalDate.of(2020,7,30)).withId(targetId).build();
        TargetDetail detailThree = TestTargetDetailBuilder.createValidTargetDetail().withValueDate(LocalDate.of(2020,7,1)).withId(targetId).build();
        TargetDetail detailFour = TestTargetDetailBuilder.createValidTargetDetail().withValueDate(LocalDate.of(2020,6,11)).withId(targetId).build();

        List<TargetDetail> detailsInRepo = List.of(detailOne,detailTwo,detailThree,detailFour);

        Mockito.when(targetDetailsRepository.findAllByTargetId(targetId)).thenReturn(detailsInRepo);

        //Act
        TargetDetail result = targetsService.getLatestDetailForTargetAndDate(targetId,date);

        //Assert
        assertThat(result).hasFieldOrPropertyWithValue("valueDate",LocalDate.of(2020,7,1));
    }

    @Test
    public void getDetailForTargetAndDate_incorrectCall_incorrectTarget_IncorrectTargetIdExceptionIsThrown(){
        //Arrange
        Long targetId = 33344L;
        LocalDate date = LocalDate.of(2020,7,11);

        Mockito.when(targetDetailsRepository.findAllByTargetId(targetId)).thenReturn(Collections.emptyList());

        //Act+Assert
        assertThatExceptionOfType(IncorrectTargetIdException.class)
                .isThrownBy(() -> targetsService.getLatestDetailForTargetAndDate(targetId,date));
    }

    @Test
    public void getDetailForTargetAndDate_emptyDate_latestDetailIsReturned() throws IncorrectTargetIdException {
        //Arrange
        Long targetId = 33344L;

        TargetDetail detailOne = TestTargetDetailBuilder.createValidTargetDetail().withValueDate(LocalDate.of(2020,8,11)).withId(targetId).build();
        TargetDetail detailTwo = TestTargetDetailBuilder.createValidTargetDetail().withValueDate(LocalDate.of(2020,7,30)).withId(targetId).build();
        TargetDetail detailThree = TestTargetDetailBuilder.createValidTargetDetail().withValueDate(LocalDate.of(2020,7,1)).withId(targetId).build();
        TargetDetail detailFour = TestTargetDetailBuilder.createValidTargetDetail().withValueDate(LocalDate.of(2020,6,11)).withId(targetId).build();

        List<TargetDetail> detailsInRepo = List.of(detailOne,detailTwo,detailThree,detailFour);

        Mockito.when(targetDetailsRepository.findAllByTargetId(targetId)).thenReturn(detailsInRepo);

        //Act
        TargetDetail result = targetsService.getLatestDetailForTargetAndDate(targetId,null);

        //Assert
        assertThat(result).hasFieldOrPropertyWithValue("valueDate",LocalDate.of(2020,8,11));
    }

    @Test
    public void createNewTargetForUser_dataIsProperlyInsertedInTheTargetAndTargetDetailRepo() throws IncorrectUserIdException {
        //Arrange
        Long userId = 111L;
        Long newTargetId =-1123L;


        mockUserServiceForValidUserWithId(userId);

        Mockito.when(targetsRepository.save(Mockito.argThat(target -> target.getUser().getId().equals(userId))))
                .thenReturn(TestTargetBuilder.createValidTarget().withId(newTargetId).build());
        Mockito.when(targetDetailsRepository.findAllByTargetId(newTargetId)).thenReturn(Collections.emptyList());

        Mockito.when(dateProvider.getNow()).thenReturn(LocalDate.of(2020,2,15));
        //Act
        Target result = targetsService.createNewTargetForUser(userId);

        //Assert
        Mockito.verify(targetsRepository).save(Mockito.argThat(target -> target.getUser().getId().equals(userId)));
        Mockito.verify(targetDetailsRepository).save(Mockito.argThat(targetDetail -> targetDetail.getTarget().getId().equals(newTargetId)));
    }

    @Test
    public void createNewTargetForUser_returnedTargetHasIdAfterSave() throws IncorrectUserIdException {
        //Arrange
        Long userId = 111L;
        Long newTargetId =-1123L;


        mockUserServiceForValidUserWithId(userId);

        Mockito.when(targetsRepository.save(Mockito.argThat(target -> target.getUser().getId().equals(userId))))
                .thenReturn(TestTargetBuilder.createValidTarget().withId(newTargetId).build());
        Mockito.when(targetDetailsRepository.findAllByTargetId(newTargetId)).thenReturn(Collections.emptyList());

        Mockito.when(dateProvider.getNow()).thenReturn(LocalDate.of(2020,2,15));
        //Act
        Target result = targetsService.createNewTargetForUser(userId);

        //Assert
        assertThat(result.getId()).isEqualTo(newTargetId);
    }

    @Test
    public void createNewTargetForUser_incorrectUserId_IncorrectUserIdExceptionIsThrown() {
        //Arrange
        Long userId = 111L;

        Mockito.when(userService.existsById(userId)).thenReturn(false);

        //Act+Assert
        assertThatExceptionOfType(IncorrectUserIdException.class)
                .isThrownBy(() ->targetsService.createNewTargetForUser(userId));
    }

    @Test
    public void saveTargetAmount_detailWithNewAmountIsCorrectlySaved() throws IncorrectTargetIdException, IncorrectTargetAmountException {
        //Arrange
        Long targetId = 234L;
        Double amountToInsert = 5432.00;

        Mockito.when(targetsRepository.findById(targetId))
                .thenReturn(Optional.of(TestTargetBuilder.createValidTarget().withId(targetId).build()));
        Mockito.when(dateProvider.getNow()).thenReturn(LocalDate.of(2020,10,15));

        //Act
        targetsService.saveTargetAmount(targetId,amountToInsert);

        //Assert
        Mockito.verify(targetDetailsRepository)
                .save(Mockito.argThat(targetDetail -> targetDetail.getTarget().getId().equals(targetId)&&targetDetail.getAmount().equals(amountToInsert)));

    }
    @Test
    public void saveTargetAmount_dateOfNewDetailIsStartOfMonth() throws IncorrectTargetIdException, IncorrectTargetAmountException {
        //Arrange
        Long targetId = 234L;
        Double amountToInsert = 5432.00;

        Mockito.when(targetsRepository.findById(targetId))
                .thenReturn(Optional.of(TestTargetBuilder.createValidTarget().withId(targetId).build()));
        Mockito.when(dateProvider.getNow()).thenReturn(LocalDate.of(2020,10,15));

        //Act
        targetsService.saveTargetAmount(targetId,amountToInsert);

        //Assert
        ArgumentCaptor<TargetDetail> elementToSave = ArgumentCaptor.forClass(TargetDetail.class);
        Mockito.verify(targetDetailsRepository).save(elementToSave.capture());
        assertThat(elementToSave.getValue().getValueDate()).isEqualTo(LocalDate.of(2020,10,1));
    }

    @Test
    public void saveTargetAmount_incorrectTargetId_IncorrectTargetIdExceptionIsThrown(){
        //Arrange
        Long incorrectTargetId = 234L;
        Double amountToInsert = 5432.00;

        Mockito.when(targetsRepository.findById(incorrectTargetId))
                .thenReturn(Optional.empty());

        //Act+Assert
        assertThatExceptionOfType(IncorrectTargetIdException.class)
                .isThrownBy(() -> targetsService.saveTargetAmount(incorrectTargetId,amountToInsert));
    }

    @Test
    public void saveTargetAmount_negativeAmountIsPassed_IncorrectTargetIdExceptionIsThrown(){

        //Arrange
        Long targetId = 234L;
        Double negativeAmount = -5432.00;

        Mockito.when(targetsRepository.findById(targetId))
                .thenReturn(Optional.of(TestTargetBuilder.createValidTarget().withId(targetId).build()));

        //Act+Assert
        assertThatExceptionOfType(IncorrectTargetAmountException.class)
                .isThrownBy(() -> targetsService.saveTargetAmount(targetId,negativeAmount));

    }

    @Test
    public void deleteTargetsByIds_allPassedTargetsAreDeleted(){

        //Arrange
        List<Long> listOfTargetIds = List.of(1234L,5555L,77777L,999999L);

        //Act
        targetsService.deleteTargetsByIds(listOfTargetIds);

        //Assert
        Mockito.verify(targetsRepository).deleteAllByIdIn(listOfTargetIds);


    }
    @Test
    public void deleteTargetsByIds_allDetailsForPassedTargetsAreDeleted(){
        //Arrange
        List<Long> listOfTargetIds = List.of(1234L,5555L,77777L,999999L);

        //Act
        targetsService.deleteTargetsByIds(listOfTargetIds);

        //Assert
        Mockito.verify(targetDetailsRepository).deleteAllByTargetIdIn(listOfTargetIds);
    }

}
