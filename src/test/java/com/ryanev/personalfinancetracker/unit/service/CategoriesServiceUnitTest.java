package com.ryanev.personalfinancetracker.unit.service;

import com.ryanev.personalfinancetracker.data.repo.categories.CategoriesRepository;
import com.ryanev.personalfinancetracker.data.repo.users.UserRepository;
import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import com.ryanev.personalfinancetracker.data.entities.User;
import com.ryanev.personalfinancetracker.exceptions.IncorrectCategoryIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.exceptions.InvalidCategoryException;
import com.ryanev.personalfinancetracker.services.categories.CategoriesService;
import com.ryanev.personalfinancetracker.services.categories.CategoriesServiceImpl;
import com.ryanev.personalfinancetracker.services.categories.CategoryChangeNotifier;
import com.ryanev.personalfinancetracker.services.crud_observer.CrudChangeNotifier;
import com.ryanev.personalfinancetracker.services.dto.categories.CategoryDTO;
import com.ryanev.personalfinancetracker.services.movements.MovementChangeNotifier;
import com.ryanev.personalfinancetracker.util.TestCategoryBuilder;
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
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CategoriesServiceUnitTest {

    @InjectMocks
    CategoriesService categoriesService = new CategoriesServiceImpl();

    @Mock
    UserRepository userRepository;

    @Mock
    CategoriesRepository categoriesRepository;

    @Mock
    CategoryChangeNotifier categoryChangeNotifier;


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
    private void mockCategorySaveToReturnPassedObject(){
        Mockito.when(categoriesRepository.save(Mockito.any())).thenAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                return invocation.getArguments()[0];
            }
        });
    }


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

    @ParameterizedTest
    @ValueSource(ints = {4,7,44,23,10,0})
    public void getActiveCategoriesForUser_CorrectUser_ReturnsOnlyActiveCategories(Integer numberOfActiveCategoriesForUser){
        //Arrange
        Long userId = 333L;
        List<MovementCategory> activeCategories = Collections.nCopies(numberOfActiveCategoriesForUser,TestCategoryBuilder.createValidCategory().withFlagActive(true).build());
        List<MovementCategory> inactiveCategories =Collections.nCopies(10,TestCategoryBuilder.createValidCategory().withFlagActive(false).build());

        List<MovementCategory> allCategories = Stream.concat(activeCategories.stream(),inactiveCategories.stream()).collect(Collectors.toList());


        Mockito.when(categoriesRepository.findAllByUserId(userId))
                .thenReturn(allCategories);
        //Act
        Integer result = categoriesService.getActiveCategoriesForUser(userId).size();

        //Assert
        assertThat(result).isEqualTo(numberOfActiveCategoriesForUser);
    }

    @Test
    public void getCategoryById_CorrectId_ReturnsCorrectCategory(){
        //Arrange
        Long categoryId = -42L;
        MovementCategory categoryToBeReturned = TestCategoryBuilder.createValidCategory().withId(categoryId).build();

        Mockito.when(categoriesRepository.findById(categoryId)).thenReturn(Optional.of(categoryToBeReturned));
        //Act

        CategoryDTO result = categoriesService.getCategoryById(categoryId);

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
        CategoryDTO dtoPassed = TestCategoryBuilder.createValidCategory().withId(null).buildDTO();

        mockValidUser(dtoPassed.getUserId());
        mockCategorySaveToReturnPassedObject();
        //Act
        categoriesService.saveCategory(dtoPassed);

        //Assert
        ArgumentCaptor<MovementCategory> elementToSave = ArgumentCaptor.forClass(MovementCategory.class);
        Mockito.verify(categoriesRepository).save(elementToSave.capture());
        assertThat(elementToSave.getValue().getId()).isEqualTo(dtoPassed.getId());
        assertThat(elementToSave.getValue().getName()).isEqualTo(dtoPassed.getName());
        assertThat(elementToSave.getValue().getUser().getId()).isEqualTo(dtoPassed.getUserId());
        assertThat(elementToSave.getValue().getFlagActive()).isEqualTo(dtoPassed.getFlagActive());
        assertThat(elementToSave.getValue().getDescription()).isEqualTo(dtoPassed.getDescription());
        assertThat(elementToSave.getValue().getFallbackCategoryId()).isEqualTo(dtoPassed.getFallbackCategoryId());

    }

    @Test
    public void saveCategory_CorrectCategory_SuccessfullyUpdatesInRepo() throws InvalidCategoryException {
        //Arrange
        Long categoryId = 111L;
        CategoryDTO dtoPassed = TestCategoryBuilder
                .createValidCategory()
                .withId(categoryId)
                .withName("SomeCategoryName")
                .withDescription("SomeDescription")
                .buildDTO();

        MovementCategory oldCategoryEntry = TestCategoryBuilder
                .createValidCategory()
                .withId(categoryId)
                .withName("Old Name")
                .withDescription("OldDescription").build();

        mockValidUser(dtoPassed.getUserId());
        mockCategorySaveToReturnPassedObject();
        Mockito.when(categoriesRepository.findById(categoryId)).thenReturn(Optional.of(oldCategoryEntry));
        //Act
        categoriesService.saveCategory(dtoPassed);

        //Assert
        ArgumentCaptor<MovementCategory> elementToSave = ArgumentCaptor.forClass(MovementCategory.class);
        Mockito.verify(categoriesRepository).save(elementToSave.capture());
        assertThat(elementToSave.getValue().getId()).isEqualTo(dtoPassed.getId());
        assertThat(elementToSave.getValue().getName()).isEqualTo(dtoPassed.getName());
        assertThat(elementToSave.getValue().getUser().getId()).isEqualTo(dtoPassed.getUserId());
        assertThat(elementToSave.getValue().getFlagActive()).isEqualTo(dtoPassed.getFlagActive());
        assertThat(elementToSave.getValue().getDescription()).isEqualTo(dtoPassed.getDescription());
        assertThat(elementToSave.getValue().getFallbackCategoryId()).isEqualTo(dtoPassed.getFallbackCategoryId());

    }

    @Test
    public void saveCategory_InvalidCategory_nullCategory_InvalidCategoryExceptionIsThrown(){
        //Arrange
        CategoryDTO nullCategory = null;

        //Act+Assert
        assertThatExceptionOfType(InvalidCategoryException.class)
                .isThrownBy(() -> categoriesService.saveCategory(nullCategory))
                .withMessageContaining("Category cannot be null");
    }

    @Test
    public void saveCategory_InvalidCategory_nullUserId_InvalidCategoryExceptionIsThrown(){
        //Arrange
        CategoryDTO categoryWithNullUserId = TestCategoryBuilder
                .createValidCategory()
                .withUser(TestUserBuilder
                        .createValidUser()
                        .withId(null)
                        .build())
                .buildDTO();

        //Act+Assert
        assertThatExceptionOfType(InvalidCategoryException.class)
                .isThrownBy(() -> categoriesService.saveCategory(categoryWithNullUserId))
                .withMessageContaining("User cannot be null");
    }

    @Test
    public void saveCategory_InvalidCategory_incorrectUser_InvalidCategoryExceptionIsThrown(){
        //Arrange
        Long userId = 1337L;
        User incorrectUser = TestUserBuilder.createValidUser().withId(userId).build();
        CategoryDTO categoryWithIncorrectUserId = TestCategoryBuilder.createValidCategory().withUser(incorrectUser).buildDTO();
        Mockito.when(userRepository.existsById(userId)).thenReturn(false);
        //Act+Assert
        assertThatExceptionOfType(InvalidCategoryException.class)
                .isThrownBy(() -> categoriesService.saveCategory(categoryWithIncorrectUserId))
                .withMessageContaining("User not found");
    }

    @Test
    public void saveCategory_InvalidCategory_categoryNameAlreadyExists_InvalidCategoryExceptionIsThrown(){
        //Arrange
        Long userId = 444L;
        Long categoryId = null;
        String duplicateName = "Salary";

        User categoryUser = TestUserBuilder.createValidUser().withId(userId).build();

        CategoryDTO categoryToBeSaved = TestCategoryBuilder
                .createValidCategory()
                .withId(categoryId)
                .withName(duplicateName)
                .withUser(categoryUser).buildDTO();

        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(categoryUser));
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
        CategoryDTO categoryWithEmptyName = TestCategoryBuilder.createValidCategory().withName(emptyString).buildDTO();
        Mockito.when(userRepository.existsById(categoryWithEmptyName.getUserId())).thenReturn(true);
        //Act+Assert
        assertThatExceptionOfType(InvalidCategoryException.class)
                .isThrownBy(() -> categoriesService.saveCategory(categoryWithEmptyName))
                .withMessageContaining("Name cannot be blank");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true,false})
    public void changeCategoryFlagActive_correctCategory_categoryIsSuccessfullySet(Boolean flagValue) throws IncorrectCategoryIdException {

        //Arrange
        Long categoryId = 777L;
        MovementCategory categoryToModify = TestCategoryBuilder.createValidCategory().withId(categoryId).withFlagActive(!flagValue).build();
        Mockito.when(categoriesRepository.findById(categoryId)).thenReturn(Optional.of(categoryToModify));
        //Act
        categoriesService.changeCategoryFlagActive(categoryId,flagValue);
        //Assert
        Mockito.verify(categoriesRepository).save(Mockito.argThat(category -> category.getFlagActive().equals(flagValue)));

    }

    @ParameterizedTest
    @ValueSource(booleans = {true,false})
    public void changeCategoryFlagActive_incorrectCategoryId_IncorrectCategoryIdExceptionIsThrown(Boolean flagValue) {

        //Arrange
        Long categoryId = 777L;
        MovementCategory categoryToModify = TestCategoryBuilder.createValidCategory().withId(categoryId).withFlagActive(!flagValue).build();
        Mockito.when(categoriesRepository.findById(categoryId)).thenThrow(NoSuchElementException.class);
        //Act + Assert
        assertThatExceptionOfType(IncorrectCategoryIdException.class).isThrownBy(() -> categoriesService.changeCategoryFlagActive(categoryId,flagValue));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void createDefaultCategoriesForUser_correctUserId_noDefaultCategoriesPresent_defaultCategoriesAreCorrectlyInserted() throws IncorrectUserIdException {
        //Arrange
        Long userId = 444L;

        List<MovementCategory> listOfPresentCategories =
                List.of(TestCategoryBuilder.createValidCategory().withName("This is not a default category").build());
        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(TestUserBuilder.createValidUser().withId(userId).build()));
        Mockito.when(categoriesRepository.findAllByUserId(userId)).thenReturn(listOfPresentCategories);

        //Act
        categoriesService.createDefaultCategoriesForUser(userId);
        //Assert
        ArgumentCaptor<List> elementsToSave = ArgumentCaptor.forClass(List.class);
        Mockito.verify(categoriesRepository).saveAll(elementsToSave.capture());
        assertThat(elementsToSave.getValue().size()).isEqualTo(2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createDefaultCategoriesForUser_correctUserId_allDefaultCategoriesPresent_nothingIsInserted() throws IncorrectUserIdException {
        //Arrange
        Long userId = 444L;
        List<MovementCategory> listOfPresentCategories =
                List.of(TestCategoryBuilder.createValidCategory().withName("Income(DEFAULT)").build(),
                        TestCategoryBuilder.createValidCategory().withName("Other Expenses(DEFAULT)").build());
        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(categoriesRepository.findAllByUserId(userId)).thenReturn(listOfPresentCategories);

        //Act
        categoriesService.createDefaultCategoriesForUser(userId);
        //Assert
        ArgumentCaptor<List> elementsToSave = ArgumentCaptor.forClass(List.class);
        Mockito.verify(categoriesRepository).saveAll(elementsToSave.capture());
        assertThat(elementsToSave.getValue().size()).isEqualTo(0);
    }

    @Test
    public void createDefaultCategoriesForUser_incorrectUserId_IncorrectUserIdExceptionIsThrown(){
        //Arrange
        Long userId = 444L;

        Mockito.when(userRepository.existsById(userId)).thenReturn(false);

        //Act + Assert
        assertThatExceptionOfType(IncorrectUserIdException.class).isThrownBy(()->categoriesService.createDefaultCategoriesForUser(userId));
        //Assert
    }

    @Test
    public void getDefaultCategoriesForUser_correctUserId_onlyDefaultCategoriesAreReturned(){
        //Arrange
        Long userId = 4568L;
        MovementCategory defaultCategoryIncome = TestCategoryBuilder.createValidCategory().withName("Income(DEFAULT)").build();
        MovementCategory defaultCategoryExpenses = TestCategoryBuilder.createValidCategory().withName("Other Expenses(DEFAULT)").build();
        List<MovementCategory> listOfPresentCategories =
                List.of(defaultCategoryIncome,
                        defaultCategoryExpenses,
                        TestCategoryBuilder.createValidCategory().withName("ThisIsNotDefault").build(),
                        TestCategoryBuilder.createValidCategory().withName("ThisIsAlsoNotDefault").build());

        Mockito.when(categoriesRepository.findAllByUserId(userId)).thenReturn(listOfPresentCategories);
        //Act
        List<CategoryDTO> result = categoriesService.getDefaultCategoriesForUser(userId);
        //Assert
        assertThat(result).hasSize(2);
        assertThat(result).anySatisfy(dto -> assertThat(dto.getName().equals(defaultCategoryIncome.getName())));
        assertThat(result).anySatisfy(dto -> assertThat(dto.getName().equals(defaultCategoryExpenses.getName())));
    }


    //deleteCategoryById
    @Test
    public void deleteCategoryById_correctCategoryId_categoryIsSuccessfullyDeleted() throws IncorrectCategoryIdException {
        //Arrange
        Long fallbackCategoryId = 777L;
        Long categoryToDeleteId = 888L;

        MovementCategory categoryToDelete = TestCategoryBuilder
                .createValidCategory()
                .withId(categoryToDeleteId)
                .withFallbackCategoryId(fallbackCategoryId).build();

        Mockito.when(categoriesRepository.findById(categoryToDeleteId)).thenReturn(Optional.of(categoryToDelete));

        //Act
        categoriesService.deleteCategoryById(categoryToDeleteId);
        //Assert
        Mockito.verify(categoriesRepository).deleteById(categoryToDeleteId);
    }

    @Test
    public void deleteCategoryById_correctCategoryId_categoryChangeNotifierIsTriggered() throws IncorrectCategoryIdException {
        //Arrange
        Long categoryToDeleteId = 888L;

        MovementCategory categoryToDelete = TestCategoryBuilder
                .createValidCategory()
                .withId(categoryToDeleteId)
                .build();

        Mockito.when(categoriesRepository.findById(categoryToDeleteId)).thenReturn(Optional.of(categoryToDelete));

        //Act
        categoriesService.deleteCategoryById(categoryToDeleteId);
        //Assert
        Mockito.verify(categoryChangeNotifier).notifyAllObservers(categoryToDelete, CrudChangeNotifier.NewState.DELETE);
    }


    @Test
    public void deleteCategoryById_incorrectCategoryId_IncorrectCategoryIdIsThrown(){
        //Arrange
        Long categoryToDeleteId = 888L;

        Mockito.when(categoriesRepository.findById(categoryToDeleteId)).thenThrow(NoSuchElementException.class);

        //Act + Assert
        assertThatExceptionOfType(IncorrectCategoryIdException.class)
                .isThrownBy(() ->categoriesService.deleteCategoryById(categoryToDeleteId));

    }

}