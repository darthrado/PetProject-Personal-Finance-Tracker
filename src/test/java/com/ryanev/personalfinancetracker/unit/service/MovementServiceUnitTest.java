package com.ryanev.personalfinancetracker.unit.service;


import com.ryanev.personalfinancetracker.data.repo.categories.CategoriesRepository;
import com.ryanev.personalfinancetracker.data.repo.movements.MovementsRepository;
import com.ryanev.personalfinancetracker.data.entities.Movement;
import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import com.ryanev.personalfinancetracker.data.entities.User;
import com.ryanev.personalfinancetracker.data.repo.users.UserRepository;
import com.ryanev.personalfinancetracker.exceptions.InvalidMovementException;
import com.ryanev.personalfinancetracker.services.crud_observer.CrudChangeNotifier;
import com.ryanev.personalfinancetracker.services.dto.movements.MovementDTO;
import com.ryanev.personalfinancetracker.services.movements.MovementChangeNotifier;
import com.ryanev.personalfinancetracker.services.movements.MovementsService;
import com.ryanev.personalfinancetracker.services.movements.MovementsServiceImpl;
import com.ryanev.personalfinancetracker.util.TestCategoryBuilder;
import com.ryanev.personalfinancetracker.util.TestMovementBuilder;
import com.ryanev.personalfinancetracker.util.user.TestUserBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.time.LocalDate;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
public class MovementServiceUnitTest {

    @InjectMocks
    MovementsService movementsService = new MovementsServiceImpl();

    @Mock
    MovementsRepository movementsRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CategoriesRepository categoriesRepository;

    @Mock
    MovementChangeNotifier movementChangeNotifier;

    private void mockValidUser(Long userId){
        Mockito.lenient().when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.lenient().when(userRepository.findById(userId))
                .thenReturn(Optional.of(TestUserBuilder.createValidUser().withId(userId).build()));
    }
    private void mockValidUser(User user){
        Mockito.lenient().when(userRepository.existsById(user.getId())).thenReturn(true);
        Mockito.lenient().when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
    }

    private void mockValidCategory(String categoryName,Long userId){
        Mockito.lenient().when(categoriesRepository.findByUserIdAndName(userId,categoryName))
                .thenReturn(Optional.of(TestCategoryBuilder
                        .createValidCategory()
                        .withUser(TestUserBuilder.createValidUser().withId(userId).build())
                        .withName(categoryName)
                        .build()));
    }
    private void mockValidCategory(MovementCategory category){
        Mockito.lenient().when(categoriesRepository.findByUserIdAndName(category.getUser().getId(),category.getName()))
                .thenReturn(Optional.of(category));
    }

    private void mockCategorySaveToReturnPassedObject(){
        Mockito.when(movementsRepository.save(Mockito.any())).thenAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                return invocation.getArguments()[0];
            }
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {7,22,44,1,55,0})
    public void getMovementsForUser_correctUser_ReturnsTheCorrectNumberOfMovements(Integer numberOfMovementsForUser){

        //Arrange
        Long userId = 442L;
        Mockito.when(movementsRepository.findAllByUserId(userId))
                .thenReturn(Collections.nCopies(numberOfMovementsForUser,TestMovementBuilder.createValidMovement().build()));

        //Act
        Integer result = movementsService.getMovementsForUser(userId).size();
        //Assert
        assertThat(result).isEqualTo(numberOfMovementsForUser);
    }

    @Test
    public void saveMovement_correctMovement_successfullyInsertsInRepository() throws InvalidMovementException {
        //Arrange
        MovementDTO dtoPassed = TestMovementBuilder.createValidMovement().withId(null).buildDTO();

        mockValidUser(dtoPassed.getUserId());
        mockValidCategory(dtoPassed.getCategory(),dtoPassed.getUserId());
        mockCategorySaveToReturnPassedObject();

        //Act
        movementsService.saveMovement(dtoPassed);

        //Assert
        ArgumentCaptor<Movement> elementToSave = ArgumentCaptor.forClass(Movement.class);
        Mockito.verify(movementsRepository).save(elementToSave.capture());
        assertThat(elementToSave.getValue().getId()).isEqualTo(dtoPassed.getId());
        assertThat(elementToSave.getValue().getName()).isEqualTo(dtoPassed.getName());
        assertThat(elementToSave.getValue().getUser().getId()).isEqualTo(dtoPassed.getUserId());
        assertThat(elementToSave.getValue().getCategory().getName()).isEqualTo(dtoPassed.getCategory());
        assertThat(elementToSave.getValue().getDescription()).isEqualTo(dtoPassed.getDescription());
        assertThat(elementToSave.getValue().getValueDate()).isEqualTo(dtoPassed.getValueDate());
        assertThat(elementToSave.getValue().getAmount()).isEqualTo(dtoPassed.getAmount());
    }

    @Test
    public void saveMovement_correctMovement_successfullyUpdatesInRepository() throws InvalidMovementException {
        //Arrange
        Long movementId = 777L;
        MovementDTO dtoPassed = TestMovementBuilder
                .createValidMovement()
                .withId(movementId)
                .withName("SomeName")
                .withAmount(123456.00)
                .withDate(LocalDate.of(2020,1,1))
                .withCategory(TestCategoryBuilder.createValidCategory().withName("NewCategory").build())
                .buildDTO();

        Movement movementInRepo = TestMovementBuilder
                .createValidMovement()
                .withId(movementId)
                .withName("SomeOldName")
                .withAmount(65432.00)
                .withDate(LocalDate.of(2020,2,2))
                .withCategory(TestCategoryBuilder.createValidCategory().withName("OldCategory").build())
                .build();

        mockValidUser(dtoPassed.getUserId());
        mockValidCategory(dtoPassed.getCategory(),dtoPassed.getUserId());
        mockCategorySaveToReturnPassedObject();
        Mockito.when(movementsRepository.findById(movementId)).thenReturn(Optional.of(movementInRepo));

        //Act
        movementsService.saveMovement(dtoPassed);

        //Assert
        ArgumentCaptor<Movement> elementToSave = ArgumentCaptor.forClass(Movement.class);
        Mockito.verify(movementsRepository).save(elementToSave.capture());
        assertThat(elementToSave.getValue().getId()).isEqualTo(dtoPassed.getId());
        assertThat(elementToSave.getValue().getName()).isEqualTo(dtoPassed.getName());
        assertThat(elementToSave.getValue().getUser().getId()).isEqualTo(dtoPassed.getUserId());
        assertThat(elementToSave.getValue().getCategory().getName()).isEqualTo(dtoPassed.getCategory());
        assertThat(elementToSave.getValue().getDescription()).isEqualTo(dtoPassed.getDescription());
        assertThat(elementToSave.getValue().getValueDate()).isEqualTo(dtoPassed.getValueDate());
        assertThat(elementToSave.getValue().getAmount()).isEqualTo(dtoPassed.getAmount());
    }

    @Test
    public void saveMovement_incorrectMovement_nullCategory_incorrectMovementExceptionIsThrown() {

        MovementDTO movementWithNullCategory = TestMovementBuilder
                .createValidMovement()
                .withCategory(TestCategoryBuilder.createValidCategory().withName(null).build())
                .buildDTO();

        assertThatExceptionOfType(InvalidMovementException.class)
                .isThrownBy(() -> movementsService.saveMovement(movementWithNullCategory))
                .withMessageContaining("Category cannot be null");



    }

    @Test
    public void saveMovement_incorrectMovement_incorrectCategory_incorrectMovementExceptionIsThrown() {

        //Arrange
        MovementCategory categoryNotPresentInRepo = TestCategoryBuilder.createValidCategory().withName("InvalidName").build();
        MovementDTO movementWithInvalidCategory = TestMovementBuilder.createValidMovement().withCategory(categoryNotPresentInRepo).buildDTO();

        mockValidUser(movementWithInvalidCategory.getUserId());
        Mockito.when(categoriesRepository.findByUserIdAndName(movementWithInvalidCategory.getUserId(),movementWithInvalidCategory.getCategory()))
                .thenReturn(Optional.empty());

        //Act + Assert
        assertThatExceptionOfType(InvalidMovementException.class)
                .isThrownBy(() -> movementsService.saveMovement(movementWithInvalidCategory))
                .withMessageContaining("Category not found");

    }


    @Test
    public void saveMovement_incorrectMovement_incorrectUser_incorrectMovementExceptionIsThrown() {

        //Arrange
        Long userId = 777L;
        User userNotPresentInRepo = TestUserBuilder.createValidUser().withId(userId).build();
        MovementDTO movementWithInvalidUser= TestMovementBuilder.createValidMovement().withUser(userNotPresentInRepo).buildDTO();

        Mockito.when(userRepository.existsById(userId)).thenReturn(false);

        //Act + Assert
        assertThatExceptionOfType(InvalidMovementException.class)
                .isThrownBy(() -> movementsService.saveMovement(movementWithInvalidUser))
                .withMessageContaining("User not found");

    }

    @Test
    public void saveMovement_incorrectMovement_missingName_incorrectMovementExceptionIsThrown() {

        //Arrange
        MovementDTO movementWithEmptyName= TestMovementBuilder.createValidMovement().withName("").buildDTO();

        mockValidUser(movementWithEmptyName.getUserId());
        mockValidCategory(movementWithEmptyName.getCategory(),movementWithEmptyName.getUserId());

        //Act + Assert
        assertThatExceptionOfType(InvalidMovementException.class)
                .isThrownBy(() -> movementsService.saveMovement(movementWithEmptyName))
                .withMessageContaining("Movement Name cannot be blank");

    }

    @Test
    public void saveMovement_incorrectMovement_missingValueDate_incorrectMovementExceptionIsThrown() {

        //Arrange
        MovementDTO movementWithEmptyDate= TestMovementBuilder.createValidMovement().withDate(null).buildDTO();

        mockValidUser(movementWithEmptyDate.getUserId());
        mockValidCategory(movementWithEmptyDate.getCategory(),movementWithEmptyDate.getUserId());

        //Act + Assert
        assertThatExceptionOfType(InvalidMovementException.class)
                .isThrownBy(() -> movementsService.saveMovement(movementWithEmptyDate))
                .withMessageContaining("Movement Value Date cannot be empty");

    }

    @Test
    public void saveMovement_incorrectMovement_amountIsZero_incorrectMovementExceptionIsThrown() {

        //Arrange
        MovementDTO movementWithZeroAmount= TestMovementBuilder.createValidMovement().withAmount(0.00).buildDTO();

        mockValidUser(movementWithZeroAmount.getUserId());
        mockValidCategory(movementWithZeroAmount.getCategory(),movementWithZeroAmount.getUserId());

        //Act + Assert
        assertThatExceptionOfType(InvalidMovementException.class)
                .isThrownBy(() -> movementsService.saveMovement(movementWithZeroAmount))
                .withMessageContaining("Movement amount cannot be 0");

    }

    @Test
    public void getMovementById_correctId_MovementIsProperlyReturned(){
        //Arrage
        Long movementId = 678L;
        Movement movementReturnedByService = TestMovementBuilder
                .createValidMovement()
                .withId(movementId)
                .build();

        Mockito.when(movementsRepository.findById(movementId)).thenReturn(Optional.of(movementReturnedByService));

        MovementDTO resultMovement = movementsService.getMovementById(movementId);

        assertThat(resultMovement.getId()).isEqualTo(movementId);
    }

    @Test
    public void getMovementById_incorrectId_NoSuchElementExceptionIsThrown(){
        //Arrage
        Long movementId = 678L;

        Mockito.when(movementsRepository.findById(movementId)).thenReturn(Optional.empty());

        //Act + Assert
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> movementsService.getMovementById(movementId));
    }

    @Test
    public void deleteMovementById_correctId_MovementProperlyDeleted(){
        //Arrange
        Long movementId = 12345L;
        Movement movementToDelete = TestMovementBuilder.createValidMovement().withId(movementId).build();

        Mockito.when(movementsRepository.findById(movementId))
                .thenReturn(Optional.of(movementToDelete));
        //Act
        movementsService.deleteMovementById(movementId);

        //Assert
        Mockito.verify(movementsRepository).delete(movementToDelete);
    }

    @Test
    public void deleteMovementById_incorrectId_noActionIsPerformed(){
        //Arrange
        Long movementId = 12345L;
        Mockito.when(movementsRepository.findById(movementId)).thenReturn(Optional.empty());
        //Act
        movementsService.deleteMovementById(movementId);

        //Assert
        Mockito.verify(movementsRepository,Mockito.times(0)).delete(Mockito.any());
    }

    @Test
    public void saveMovement_correctMovement_changeNotifierCalledOnInsert() throws InvalidMovementException {
        //Arrange
        MovementDTO dtoPassed = TestMovementBuilder.createValidMovement().withId(null).buildDTO();
        Movement savedMovementObj = TestMovementBuilder.createValidMovement().build();

        mockValidUser(dtoPassed.getUserId());
        mockValidCategory(dtoPassed.getCategory(),dtoPassed.getUserId());
        Mockito.when(movementsRepository.save(Mockito.any()))
                .thenReturn(savedMovementObj);

        //Act
        movementsService.saveMovement(dtoPassed);

        //Assert
        Mockito.verify(movementChangeNotifier).notifyAllObservers(savedMovementObj, CrudChangeNotifier.NewState.CREATE);
    }
    @Test
    public void saveMovement_correctMovement_changeNotifierCalledOnUpdate() throws InvalidMovementException {
        //Arrange
        Long movementId = 666L;
        MovementDTO dtoPassed = TestMovementBuilder.createValidMovement().withId(movementId).buildDTO();
        Movement savedMovementObj = TestMovementBuilder.createValidMovement().withId(movementId).build();

        mockValidUser(dtoPassed.getUserId());
        mockValidCategory(dtoPassed.getCategory(),dtoPassed.getUserId());
        Mockito.when(movementsRepository.findById(movementId)).thenReturn(Optional.of(savedMovementObj));
        Mockito.when(movementsRepository.save(Mockito.any()))
                .thenReturn(savedMovementObj);

        //Act
        movementsService.saveMovement(dtoPassed);

        //Assert
        Mockito.verify(movementChangeNotifier).notifyAllObservers(savedMovementObj, CrudChangeNotifier.NewState.UPDATE);
    }
    @Test
    public void saveMovement_correctMovement_changeNotifierCalledOnDelete(){
        //Arrange
        Long movementId = 12345L;
        Movement movementToDelete = TestMovementBuilder.createValidMovement().withId(movementId).build();

        Mockito.when(movementsRepository.findById(movementId))
                .thenReturn(Optional.of(movementToDelete));
        //Act
        movementsService.deleteMovementById(movementId);

        //Assert
        Mockito.verify(movementChangeNotifier).notifyAllObservers(movementToDelete, CrudChangeNotifier.NewState.DELETE);
    }

}
