//package com.ryanev.personalfinancetracker.unit.service;
//
//
//import com.ryanev.personalfinancetracker.data.repo.movements.MovementsRepository;
//import com.ryanev.personalfinancetracker.data.entities.Movement;
//import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
//import com.ryanev.personalfinancetracker.data.entities.User;
//import com.ryanev.personalfinancetracker.exceptions.InvalidMovementException;
//import com.ryanev.personalfinancetracker.services.categories.CategoriesService;
//import com.ryanev.personalfinancetracker.services.movements.MovementsService;
//import com.ryanev.personalfinancetracker.services.users.UserService;
//import com.ryanev.personalfinancetracker.services.movements.DefaultMovementsService;
//import com.ryanev.personalfinancetracker.util.TestCategoryBuilder;
//import com.ryanev.personalfinancetracker.util.TestMovementBuilder;
//import com.ryanev.personalfinancetracker.util.user.TestUserBuilder;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.ValueSource;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Collections;
//import java.util.NoSuchElementException;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
//
//@ExtendWith(MockitoExtension.class)
//public class MovementServiceUnitTest {
//
//    @InjectMocks
//    MovementsService movementsService = new DefaultMovementsService();
//
//    @Mock
//    MovementsRepository movementsRepository;
//
//    @Mock
//    UserService userService;
//
//    @Mock
//    CategoriesService categoriesService;
//
//
//    @ParameterizedTest
//    @ValueSource(ints = {7,22,44,1,55,0})
//    public void getMovementsForUser_correctUser_ReturnsTheCorrectNumberOfMovements(Integer numberOfMovementsForUser){
//
//        //Arrange
//        Long userId = 442L;
//        Mockito.when(movementsRepository.findAllByUserId(userId))
//                .thenReturn(Collections.nCopies(numberOfMovementsForUser,TestMovementBuilder.createValidMovement().build()));
//
//        //Act
//        Integer result = movementsService.getMovementsForUser(userId).size();
//        //Assert
//        assertThat(result).isEqualTo(numberOfMovementsForUser);
//    }
//
//    @Test
//    public void saveMovement_correctMovement_successfullyInsertsInRepository() throws InvalidMovementException {
//        //Arrange
//        Movement correctMovement = TestMovementBuilder.createValidMovement().build();
//        Mockito.when(userService.existsById(correctMovement.getUser().getId())).thenReturn(true);
//        Mockito.when(categoriesService.existsById(correctMovement.getCategory().getId())).thenReturn(true);
//        //Act
//        movementsService.saveMovement(correctMovement);
//
//        //Assert
//        Mockito.verify(movementsRepository).save(correctMovement);
//    }
//
//
//    @Test
//    public void saveMovement_incorrectMovement_nullCategory_incorrectMovementExceptionIsThrown() {
//
//        Movement movementWithNullCategory = TestMovementBuilder.createValidMovement().withCategory(null).build();
//
//        assertThatExceptionOfType(InvalidMovementException.class)
//                .isThrownBy(() -> movementsService.saveMovement(movementWithNullCategory))
//                .withMessageContaining("Category cannot be null");
//
//
//
//    }
//
//    @Test
//    public void saveMovement_incorrectMovement_incorrectCategory_incorrectMovementExceptionIsThrown() {
//
//        //Arrange
//        Long categoryId = 234L;
//        MovementCategory categoryNotPresentInRepo = TestCategoryBuilder.createValidCategory().withId(categoryId).build();
//        Movement movementWithInvalidCategory = TestMovementBuilder.createValidMovement().withCategory(categoryNotPresentInRepo).build();
//
//        Mockito.when(userService.existsById(movementWithInvalidCategory.getUser().getId())).thenReturn(true);
//        Mockito.when(categoriesService.existsById(categoryId)).thenReturn(false);
//
//        //Act + Assert
//        assertThatExceptionOfType(InvalidMovementException.class)
//                .isThrownBy(() -> movementsService.saveMovement(movementWithInvalidCategory))
//                .withMessageContaining("Category not found");
//
//    }
//
//
//    @Test
//    public void saveMovement_incorrectMovement_nullUser_incorrectMovementExceptionIsThrown() {
//
//        Movement movementWithNullUser = TestMovementBuilder.createValidMovement().withUser(null).build();
//
//        assertThatExceptionOfType(InvalidMovementException.class)
//                .isThrownBy(() -> movementsService.saveMovement(movementWithNullUser))
//                .withMessageContaining("User cannot be null");
//
//
//
//    }
//
//    @Test
//    public void saveMovement_incorrectMovement_incorrectUser_incorrectMovementExceptionIsThrown() {
//
//        //Arrange
//        Long userId = 777L;
//        User userNotPresentInRepo = TestUserBuilder.createValidUser().withId(userId).build();
//        Movement movementWithInvalidUser= TestMovementBuilder.createValidMovement().withUser(userNotPresentInRepo).build();
//
//        Mockito.when(userService.existsById(userId)).thenReturn(false);
//
//        //Act + Assert
//        assertThatExceptionOfType(InvalidMovementException.class)
//                .isThrownBy(() -> movementsService.saveMovement(movementWithInvalidUser))
//                .withMessageContaining("User not found");
//
//    }
//
//    @Test
//    public void saveMovement_incorrectMovement_missingName_incorrectMovementExceptionIsThrown() {
//
//        //Arrange
//        Movement movementWithEmptyName= TestMovementBuilder.createValidMovement().withName("").build();
//
//        Mockito.when(userService.existsById(movementWithEmptyName.getUser().getId())).thenReturn(true);
//        Mockito.when(categoriesService.existsById(movementWithEmptyName.getCategory().getId())).thenReturn(true);
//
//        //Act + Assert
//        assertThatExceptionOfType(InvalidMovementException.class)
//                .isThrownBy(() -> movementsService.saveMovement(movementWithEmptyName))
//                .withMessageContaining("Movement Name cannot be blank");
//
//    }
//
//    @Test
//    public void saveMovement_incorrectMovement_missingValueDate_incorrectMovementExceptionIsThrown() {
//
//        //Arrange
//        Movement movementWithEmptyDate= TestMovementBuilder.createValidMovement().withDate(null).build();
//
//        Mockito.when(userService.existsById(movementWithEmptyDate.getUser().getId())).thenReturn(true);
//        Mockito.when(categoriesService.existsById(movementWithEmptyDate.getCategory().getId())).thenReturn(true);
//
//        //Act + Assert
//        assertThatExceptionOfType(InvalidMovementException.class)
//                .isThrownBy(() -> movementsService.saveMovement(movementWithEmptyDate))
//                .withMessageContaining("Movement Value Date cannot be empty");
//
//    }
//
//    @Test
//    public void saveMovement_incorrectMovement_amountIsZero_incorrectMovementExceptionIsThrown() {
//
//        //Arrange
//        Movement movementWithEmptyName= TestMovementBuilder.createValidMovement().withAmount(0.00).build();
//
//        Mockito.when(userService.existsById(movementWithEmptyName.getUser().getId())).thenReturn(true);
//        Mockito.when(categoriesService.existsById(movementWithEmptyName.getCategory().getId())).thenReturn(true);
//
//        //Act + Assert
//        assertThatExceptionOfType(InvalidMovementException.class)
//                .isThrownBy(() -> movementsService.saveMovement(movementWithEmptyName))
//                .withMessageContaining("Movement amount cannot be 0");
//
//    }
//
//    @Test
//    public void getMovementById_correctId_MovementIsProperlyReturned(){
//        //Arrage
//        Long movementId = 678L;
//        Movement movementReturnedByService = TestMovementBuilder.createValidMovement().withId(movementId).build();
//
//        Mockito.when(movementsRepository.findById(movementId)).thenReturn(Optional.of(movementReturnedByService));
//
//        Movement resultMovement = movementsService.getMovementById(movementId);
//
//        assertThat(resultMovement.getId()).isEqualTo(movementId);
//    }
//
//    @Test
//    public void getMovementById_incorrectId_NoSuchElementExceptionIsThrown(){
//        //Arrage
//        Long movementId = 678L;
//
//        Mockito.when(movementsRepository.findById(movementId)).thenReturn(Optional.empty());
//
//        //Act + Assert
//        assertThatExceptionOfType(NoSuchElementException.class)
//                .isThrownBy(() -> movementsService.getMovementById(movementId));
//    }
//
//    @Test
//    public void deleteMovementById_correctId_MovementProperlyDeleted(){
//        //Arrange
//        Long movementId = 12345L;
//        Mockito.when(movementsRepository.existsById(movementId)).thenReturn(true);
//        //Act
//        movementsService.deleteMovementById(movementId);
//
//        //Assert
//        Mockito.verify(movementsRepository).deleteById(movementId);
//    }
//
//    @Test
//    public void deleteMovementById_incorrectId_noActionIsPerformed(){
//        //Arrange
//        Long movementId = 12345L;
//        Mockito.when(movementsRepository.existsById(movementId)).thenReturn(false);
//        //Act
//        movementsService.deleteMovementById(movementId);
//
//        //Assert
//        Mockito.verify(movementsRepository,Mockito.times(0)).deleteById(movementId);
//    }
//
////
////    getMovementById
////  - verify correct movement is selected by the service
////  - verify an exception is thrown if no movement is found
////
////    deleteMovementById
////  - verify correct movement is deleted
////  - verify exception is thrown if movement not found
//
//
//}
