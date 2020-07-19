package com.ryanev.personalfinancetracker.unit.service;

import com.ryanev.personalfinancetracker.dao.CategoriesRepository;
import com.ryanev.personalfinancetracker.dao.UserRepository;
import com.ryanev.personalfinancetracker.entities.MovementCategory;
import com.ryanev.personalfinancetracker.entities.User;
import com.ryanev.personalfinancetracker.exceptions.InvalidCategoryException;
import com.ryanev.personalfinancetracker.services.CategoriesService;
import com.ryanev.personalfinancetracker.services.implementation.DefaultCategoriesService;
import com.ryanev.personalfinancetracker.util.TestCategoryBuilder;
import com.ryanev.personalfinancetracker.util.TestUserBuilder;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CategoriesServiceUnitTest {

    @InjectMocks
    CategoriesService categoriesService = new DefaultCategoriesService();

    @Mock
    UserRepository userRepository;

    @Mock
    CategoriesRepository categoriesRepository;

    @ParameterizedTest
    @ValueSource(ints = {4,7,44,23,10,0})
    public void getCategoriesForUser_CorrectUser_ReturnsCorrectNumberOfCategories(Integer numberOfCategoriesForUser){
        //Arrange
        Long userId = 333L;
        Mockito.when(categoriesRepository.findAllByUserId(userId))
                .thenReturn(Collections.nCopies(numberOfCategoriesForUser,TestCategoryBuilder.createValidCategory().build()));
        //Act
        Integer result = categoriesService.getCategoriesForUser(userId).size();

        //Assert
        assertThat(result).isEqualTo(numberOfCategoriesForUser);
    }



    @Test
    public void getCategoryById_CorrectId_ReturnsCorrectCategory(){
        //Arrange
        Long categoryId = -42L;
        MovementCategory categoryToBeReturned = TestCategoryBuilder.createValidCategory().withId(categoryId).build();

        Mockito.when(categoriesRepository.findById(categoryId)).thenReturn(Optional.of(categoryToBeReturned));
        //Act

        MovementCategory result = categoriesService.getCategoryById(categoryId);

        //Assert
        assertThat(result.getId()).isEqualTo(categoryId);
    }

    @Test
    public void getCategoryById_IncorrectId_NoSuchElementExceptionIsThrown(){
        //Arrange
        Long categoryId = 385L;
        Mockito.when(categoriesRepository.findById(categoryId)).thenReturn(Optional.empty());

        //Act+Assert
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> categoriesService.getCategoryById(categoryId));
    }

    @Test
    public void saveCategory_CorrectCategory_SuccessfullyInsertsInRepo() throws InvalidCategoryException {
        //Arrange
        MovementCategory categoryToBeSaved = TestCategoryBuilder.createValidCategory().build();
        Mockito.when(userRepository.existsById(categoryToBeSaved.getUser().getId())).thenReturn(true);
        //Act
        categoriesService.saveCategory(categoryToBeSaved);

        //Assert
        Mockito.verify(categoriesRepository).save(categoryToBeSaved);
    }

    @Test
    public void saveCategory_InvalidCategory_nullCategory_InvalidCategoryExceptionIsThrown(){
        //Arrange
        MovementCategory nullCategory = null;

        //Act+Assert
        assertThatExceptionOfType(InvalidCategoryException.class)
                .isThrownBy(() -> categoriesService.saveCategory(nullCategory))
                .withMessageContaining("Category cannot be null");
    }

    @Test
    public void saveCategory_InvalidCategory_nullUser_InvalidCategoryExceptionIsThrown(){
        //Arrange
        MovementCategory categoryWithNullUser = TestCategoryBuilder.createValidCategory().withUser(null).build();

        //Act+Assert
        assertThatExceptionOfType(InvalidCategoryException.class)
                .isThrownBy(() -> categoriesService.saveCategory(categoryWithNullUser))
                .withMessageContaining("User cannot be null");
    }

    @Test
    public void saveCategory_InvalidCategory_incorrectUser_InvalidCategoryExceptionIsThrown(){
        //Arrange
        Long userId = 1337L;
        User incorrectUser = TestUserBuilder.createValidUser().withId(userId).build();
        MovementCategory categoryWithIncorrectUser = TestCategoryBuilder.createValidCategory().withUser(incorrectUser).build();
        Mockito.when(userRepository.existsById(userId)).thenReturn(false);
        //Act+Assert
        assertThatExceptionOfType(InvalidCategoryException.class)
                .isThrownBy(() -> categoriesService.saveCategory(categoryWithIncorrectUser))
                .withMessageContaining("User not found");
    }

    @Test
    public void saveCategory_InvalidCategory_categoryNameAlreadyExists_InvalidCategoryExceptionIsThrown(){
        //Arrange
        Long userId = 444L;
        String duplicateName = "Salary";
        MovementCategory categoryToBeSaved = TestCategoryBuilder.createValidCategory().withName(duplicateName).build();
        Mockito.when(userRepository.existsById(categoryToBeSaved.getUser().getId())).thenReturn(true);
        Mockito.when(categoriesRepository.save(Mockito.argThat(category -> category.getName().equals(duplicateName))))
                .thenThrow(DataIntegrityViolationException.class);
        //Act+Assert
        assertThatExceptionOfType(InvalidCategoryException.class)
                .isThrownBy(() -> categoriesService.saveCategory(categoryToBeSaved))
                .withMessageContaining("Save would violate data integrity");


    }

    @ParameterizedTest
    @ValueSource(strings = {""," ","    "})
    public void saveCategory_InvalidCategory_nameIsEmpty_InvalidCategoryExceptionIsThrown(String emptyString){
        //Arrange
        MovementCategory categoryWithEmptyName = TestCategoryBuilder.createValidCategory().withName(emptyString).build();
        Mockito.when(userRepository.existsById(categoryWithEmptyName.getUser().getId())).thenReturn(true);
        //Act+Assert
        assertThatExceptionOfType(InvalidCategoryException.class)
                .isThrownBy(() -> categoriesService.saveCategory(categoryWithEmptyName))
                .withMessageContaining("Name cannot be blank");
    }
}