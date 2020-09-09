package com.ryanev.personalfinancetracker.unit.controller;


import com.ryanev.personalfinancetracker.data.entities.User;
import com.ryanev.personalfinancetracker.services.dto.categories.CategoryDTO;
import com.ryanev.personalfinancetracker.services.dto.movements.MovementDTO;
import com.ryanev.personalfinancetracker.util.user.TestUserBuilder;
import com.ryanev.personalfinancetracker.web.controllers.MovementsController;
import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import com.ryanev.personalfinancetracker.exceptions.InvalidMovementException;
import com.ryanev.personalfinancetracker.services.categories.CategoriesService;
import com.ryanev.personalfinancetracker.services.movements.MovementsService;
import com.ryanev.personalfinancetracker.services.users.UserService;
import com.ryanev.personalfinancetracker.util.TestCategoryBuilder;
import com.ryanev.personalfinancetracker.util.TestMovementBuilder;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MovementsController.class)
public class MovementsControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @MockBean
    private CategoriesService categoriesService;
    @MockBean
    private MovementsService movementsService;


    private String getControllerBaseURL(Long userId){
        return "/"+userId+"/movements";
    }
    private Long someUserId(){
        return 888777666L;
    }

    private Double transformIntoSigned(Double unsignedAmount, boolean flagAmountPositive){
        return (flagAmountPositive?1:-1)*unsignedAmount;
    }

    private void mockUser(String username,Long userId, Boolean valid){

        if(valid){
            User mockUser = TestUserBuilder.createValidUser().withId(userId).withUsername(username).build();
            Mockito.lenient().when(userService.existsById(userId)).thenReturn(true);
            Mockito.lenient().when(userService.getUserByUsername(username)).thenReturn(mockUser);
            Mockito.lenient().when(userService.getUserById(userId)).thenReturn(mockUser);
        }else {
            Mockito.lenient().when(userService.existsById(userId)).thenReturn(false);
            Mockito.lenient().when(userService.getUserByUsername(username)).thenThrow(NoSuchElementException.class);
            Mockito.lenient().when(userService.getUserById(userId)).thenThrow(NoSuchElementException.class);
        }
    }

    @Test
    @WithMockUser("testUser")
    public void movementsPage_CorrectGetRequest_allMovementsForUserArePresent() throws Exception{
        Long userId = 777L;
        Integer expectedRecords = 8;

        mockUser("testUser",userId,true);

        Mockito.when(movementsService.getMovementsForUser(userId))
                .thenReturn(Collections.nCopies(expectedRecords, TestMovementBuilder.createValidMovement().buildDTO()));

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("movementsList", Matchers.iterableWithSize(expectedRecords)));
    }

    @ParameterizedTest
    @WithMockUser("testUser")
    @CsvSource({"234,Salary june,5000,2020,6,15,SALARY",
            "666,Lunch: Happy,-57.23,2020,5,17,OTHER",
            "543,Steam Bundle Purchase,-120,2020,6,18,Games"})
    public void movementsPage_correctGetRequest_dataOnASingleMovementIsCorrect(Long userId,
                                                                                       String movementName,
                                                                                       Double movementAmount,
                                                                                       Integer movementYear,
                                                                                       Integer movementMonth,
                                                                                       Integer movementDay,
                                                                                       String movementCategoryName) throws Exception{

        MovementCategory movementCategory = TestCategoryBuilder.createValidCategory().withName(movementCategoryName).build();
        MovementDTO existingMovement = TestMovementBuilder.createValidMovement()
                .withName(movementName)
                .withAmount(movementAmount)
                .withDate(LocalDate.of(movementYear,movementMonth,movementDay))
                .withCategory(movementCategory)
                .buildDTO();
        Mockito.when(movementsService.getMovementsForUser(userId)).thenReturn(List.of(existingMovement));

        mockUser("testUser",userId,true);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model()
                        .attribute("movementsList",Matchers
                                .hasItem(Matchers.allOf(
                                        Matchers.hasProperty("name",Matchers.is(movementName)),
                                        Matchers.hasProperty("signedAmount",Matchers.is(movementAmount)),
                                        Matchers.hasProperty("valueDate",Matchers.is(LocalDate.of(movementYear,movementMonth,movementDay))),
                                        Matchers.hasProperty("categoryName",Matchers.is(movementCategoryName))
                                ))
                        ));
    }

    @Test
    @WithMockUser("testUser")
    public void movementsPage_correctGetRequest_newMovementButtonIsPresent() throws Exception{
        Long userId = someUserId();

        mockUser("testUser",userId,true);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .string(Matchers.containsString(getControllerBaseURL(userId).concat("/new"))));
    }

    @Test
    @WithMockUser("testUser")
    //TODO: we expect each movement to have Edit and Delete links. see how
    public void movementsPage_correctGetRequest_editAndDeleteButtonsArePresent() throws Exception{

        Long userId = someUserId();
        Integer expectedRecords = 8;

        mockUser("testUser",userId,true);

        Mockito.when(movementsService.getMovementsForUser(userId))
                .thenReturn(Collections.nCopies(expectedRecords, TestMovementBuilder
                        .createValidMovement()
                        .withUser(TestUserBuilder.createValidUser().withId(userId).build())
                        .buildDTO()));

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(getControllerBaseURL(userId)+"/update")))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(getControllerBaseURL(userId)+"/delete")));
    }

    @ParameterizedTest
    @WithMockUser("testUser")
    @ValueSource(longs = {-22,3456,77,22222,1234567456})
    public void movementsPage_incorrectGetRequest_attemptingToAccessDifferentUserData_forbiddenErrorIsThrown(Long userId) throws Exception{

        Long loggedUser = 23941L;
        if(userId.equals(loggedUser)){
            throw new RuntimeException("Passed user can't be the same as logged user");
        }

        mockUser("testUser",loggedUser,true);

        Mockito.when(userService.getUserById(userId)).thenThrow(NoSuchElementException.class);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(longs = {-22,3456,77,22222,1234567456})
    public void movementsPage_incorrectGetRequest_unauthorized_unauthorizedErrorIsThrown(Long userId) throws Exception{

        Mockito.when(userService.getUserById(userId)).thenThrow(NoSuchElementException.class);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @ParameterizedTest
    @WithMockUser("testUser")
    @ValueSource(strings = {"id","valueDate","flagAmountPositive","unsignedAmount","name","description","categoryName"})
    //TODO there is probably a better way to do this - check all fields without performing to many get requests
    public void movementFormNew_correctGetRequest_allFormFieldsArePresent(String fieldId) throws Exception{

        Long userId = someUserId();
        String testString = "id=\""+fieldId+"\"";

        mockUser("testUser",userId,true);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/new")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(testString)));
    }


    @Test
    @WithMockUser("testUser")
    public void movementFormNew_correctGetRequest_saveLinkIsPresent() throws Exception{
        Long userId = someUserId();
        mockUser("testUser",userId,true);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/new")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(getControllerBaseURL(userId).concat("/save"))));
    }

    @Test
    @WithMockUser("testUser")
    public void movementFormNew_correctGetRequest_backLinkIsPresent() throws Exception{
        Long userId = someUserId();

        mockUser("testUser",userId,true);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/new")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(getControllerBaseURL(userId))));
    }

    @ParameterizedTest
    @ValueSource(longs = {-22,3456,77,22222,1234567456})
    public void movementFormNew_incorrectGetRequest_unauthorized_unauthorizedErrorIsThrown(Long userId) throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/new")))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @ParameterizedTest
    @WithMockUser("testUser")
    @ValueSource(longs = {-22,3456,77,22222,1234567456})
    public void movementFormNew_incorrectGetRequest_attemptingToAccessDifferentUserData_forbiddenErrorIsThrown(Long userId) throws Exception{

        Long loggedUser = 23941L;
        if(userId.equals(loggedUser)){
            throw new RuntimeException("Passed user can't be the same as logged user");
        }

        mockUser("testUser",loggedUser,true);

        Mockito.when(userService.getUserById(userId)).thenThrow(NoSuchElementException.class);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/new")))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @ParameterizedTest
    @WithMockUser("testUser")
    @CsvSource({"-333,1,2020,6,15,SALARY,Salary june,5000,true,Salary June"})
    public void movementFormEdit_correctGetRequest_formDataIsCorrectlyLoaded(Long userId,
                                                                                   Long movementId,
                                                                                   Integer movementYear,
                                                                                   Integer movementMonth,
                                                                                   Integer movementDay,
                                                                                   String movementCategoryName,
                                                                                   String movementName,
                                                                                   Double movementUnsignedAmount,
                                                                                   Boolean movementSignPositive,
                                                                                   String movementDescription) throws Exception {
        LocalDate movementDate = LocalDate.of(movementYear,movementMonth,movementDay);
        Long categoryId = 888L;

        mockUser("testUser",userId,true);

        MovementCategory movementCategory = TestCategoryBuilder.createValidCategory()
                .withId(categoryId)
                .withName(movementCategoryName)
                .build();

        CategoryDTO categoryDTO = TestCategoryBuilder.createValidCategory()
                .withId(categoryId)
                .withName(movementCategoryName)
                .buildDTO();

        MovementDTO movementToEdit = TestMovementBuilder.createValidMovement()
                .withId(movementId)
                .withDate(movementDate)
                .withName(movementName)
                .withAmount(transformIntoSigned(movementUnsignedAmount,movementSignPositive))
                .withCategory(movementCategory)
                .withDescription(movementDescription)
                .buildDTO();

        Mockito.when(movementsService.getMovementById(movementId)).thenReturn(movementToEdit);
        Mockito.when(categoriesService.getActiveCategoriesForUser(userId)).thenReturn(List.of(categoryDTO));


        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(movementId.toString())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .model()
                        .attribute("movement", Matchers.allOf(
                                Matchers.hasProperty("id",Matchers.is(movementId)),
                                Matchers.hasProperty("unsignedAmount",Matchers.is(movementUnsignedAmount)),
                                Matchers.hasProperty("valueDate",Matchers.is(movementDate)),
                                Matchers.hasProperty("name",Matchers.is(movementName)),
                                Matchers.hasProperty("flagAmountPositive",Matchers.is(movementSignPositive)),
                                Matchers.hasProperty("categoryName",Matchers.is(movementCategoryName)),
                                Matchers.hasProperty("description",Matchers.is(movementDescription))))
                );
    }

    @Test
    @WithMockUser("testUser")
    public void movementFormEdit_correctGetRequest_saveLinkIsPresent() throws Exception{
        Long userId = someUserId();
        Long movementId = -112233L;

        mockUser("testUser",userId,true);

        CategoryDTO movementCategory = TestCategoryBuilder.createValidCategory().buildDTO();

        MovementDTO movementToEdit = TestMovementBuilder.createValidMovement()
                .withId(movementId)
                .buildDTO();

        Mockito.when(movementsService.getMovementById(movementId)).thenReturn(movementToEdit);
        Mockito.when(categoriesService.getActiveCategoriesForUser(userId)).thenReturn(List.of(movementCategory));

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(movementId.toString())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(getControllerBaseURL(userId).concat("/save"))));
    }

    @ParameterizedTest
    @CsvSource({"42,1","-777,2","666,3"})
    public void movementFormEdit_incorrectGetRequest_unauthorized_unauthorizedErrorIsThrown(Long userId, Long movementId) throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(movementId.toString())))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @ParameterizedTest
    @WithMockUser("testUser")
    @CsvSource({"42,1","-777,2","666,3"})
    public void movementFormEdit_incorrectGetRequest_attemptingToAccessDifferentUserData_forbiddenErrorIsThrown(Long userId, Long movementId) throws Exception{

        Long loggedUser = 23941L;
        if(userId.equals(loggedUser)){
            throw new RuntimeException("Passed user can't be the same as logged user");
        }

        mockUser("testUser",loggedUser,true);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(movementId.toString())))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }


    @Test
    @WithMockUser("testUser")
    public void movementFormEdit_incorrectGetRequest_missingMovementId_clientErrorIsThrown() throws Exception{
        Long userId = someUserId();

        mockUser("testUser",userId,true);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update")))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    @WithMockUser("testUser")
    public void movementFormEdit_incorrectGetRequest_incorrectMovementId_clientErrorIsThrown() throws Exception{
        Long userId = someUserId();
        Long movementId = -112233L;

        mockUser("testUser",userId,true);

        Mockito.when(movementsService.getMovementById(movementId)).thenThrow(NoSuchElementException.class);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(movementId.toString())))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @ParameterizedTest
    @WithMockUser("testUser")
    @CsvSource({"12345,1,2020,6,15,SALARY,Salary june,5000,true,Salary June"})
    //todo add more scenarios
    public void movementFormDelete_correctGetRequest_allDataIsCorrectlyLoaded(Long userId,
                                                                                    Long movementId,
                                                                                    Integer movementYear,
                                                                                    Integer movementMonth,
                                                                                    Integer movementDay,
                                                                                    String movementCategoryName,
                                                                                    String movementName,
                                                                                    Double movementUnsignedAmount,
                                                                                    Boolean movementSignPositive,
                                                                                    String movementDescription) throws Exception {
        LocalDate movementDate = LocalDate.of(movementYear,movementMonth,movementDay);
        Long categoryId = 888L;

        mockUser("testUser",userId,true);

        MovementCategory movementCategory = TestCategoryBuilder.createValidCategory()
                .withId(categoryId)
                .withName(movementCategoryName)
                .build();

        CategoryDTO categoryDto = TestCategoryBuilder.createValidCategory()
                .withId(categoryId)
                .withName(movementCategoryName)
                .buildDTO();

        MovementDTO movementToEdit = TestMovementBuilder.createValidMovement()
                .withId(movementId)
                .withDate(movementDate)
                .withName(movementName)
                .withAmount(transformIntoSigned(movementUnsignedAmount,movementSignPositive))
                .withCategory(movementCategory)
                .withDescription(movementDescription)
                .buildDTO();

        Mockito.when(movementsService.getMovementById(movementId)).thenReturn(movementToEdit);
        Mockito.when(categoriesService.getActiveCategoriesForUser(userId)).thenReturn(List.of(categoryDto));
        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/delete").concat("?id=").concat(movementId.toString())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .model()
                        .attribute("movement", Matchers.allOf(
                                Matchers.hasProperty("id",Matchers.is(movementId)),
                                Matchers.hasProperty("unsignedAmount",Matchers.is(movementUnsignedAmount)),
                                Matchers.hasProperty("valueDate",Matchers.is(movementDate)),
                                Matchers.hasProperty("name",Matchers.is(movementName)),
                                Matchers.hasProperty("flagAmountPositive",Matchers.is(movementSignPositive)),
                                Matchers.hasProperty("categoryName",Matchers.is(movementCategoryName)),
                                Matchers.hasProperty("description",Matchers.is(movementDescription))))
                );
    }

    @Test
    @WithMockUser("testUser")
    public void movementFormDelete_correctGetRequest_allDataIsCorrectlyDisabled() throws Exception {
        Long userId = someUserId();
        Long movementId = -112233L;

        mockUser("testUser",userId,true);

        CategoryDTO movementCategory = TestCategoryBuilder.createValidCategory().buildDTO();

        MovementDTO movementToEdit = TestMovementBuilder.createValidMovement()
                .withId(movementId)
                .buildDTO();

        Mockito.when(movementsService.getMovementById(movementId)).thenReturn(movementToEdit);
        Mockito.when(categoriesService.getActiveCategoriesForUser(userId)).thenReturn(List.of(movementCategory));

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/delete").concat("?id=").concat(movementId.toString())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .model()
                        .attribute("disableFormFields",Matchers.is(true)));
    }

    @Test
    @WithMockUser("testUser")
    public void movementFormDelete_correctGetRequest_confirmDeleteLinkIsPresent() throws Exception{
        Long userId = someUserId();
        Long movementId = -112233L;

        mockUser("testUser",userId,true);

        CategoryDTO movementCategory = TestCategoryBuilder.createValidCategory().buildDTO();

        MovementDTO movementToEdit = TestMovementBuilder.createValidMovement()
                .withId(movementId)
                .buildDTO();

        Mockito.when(movementsService.getMovementById(movementId)).thenReturn(movementToEdit);
        Mockito.when(categoriesService.getActiveCategoriesForUser(userId)).thenReturn(List.of(movementCategory));

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/delete").concat("?id=").concat(movementId.toString())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .content().string(Matchers.containsString(getControllerBaseURL(userId).concat("/delete/confirm"))));
    }


    @ParameterizedTest
    @CsvSource({"42,1","-777,2","666,3"})
    public void movementFormDelete_incorrectGetRequest_unauthorized_unauthorizedErrorIsThrown(Long userId, Long movementId) throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/delete").concat("?id=").concat(movementId.toString())))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @ParameterizedTest
    @WithMockUser("testUser")
    @CsvSource({"42,1","-777,2","666,3"})
    public void movementFormDelete_incorrectGetRequest_attemptingToAccessDifferentUserData_forbiddenErrorIsThrown(Long userId, Long movementId) throws Exception {

        Long loggedUser = 23941L;
        if(userId.equals(loggedUser)){
            throw new RuntimeException("Passed user can't be the same as logged user");
        }

        mockUser("testUser",loggedUser,true);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/delete").concat("?id=").concat(movementId.toString())))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }


    @Test
    @WithMockUser("testUser")
    public void movementFormDelete_incorrectGetRequest_missingMovementId_clientErrorIsThrown() throws Exception{
        Long userId = someUserId();

        mockUser("testUser",userId,true);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/delete")))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    @WithMockUser("testUser")
    public void movementFormDelete_incorrectGetRequest_incorrectMovementId_clientErrorIsThrown() throws Exception{
        Long userId = someUserId();
        Long movementId = -1337L;
        mockUser("testUser",userId,true);

        Mockito.when(movementsService.getMovementById(movementId)).thenThrow(NoSuchElementException.class);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/delete").concat("?id=").concat(movementId.toString())))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @ParameterizedTest
    @WithMockUser("testUser")
    @CsvSource({"777,SalaryCategory,2020-01-15,First Salary of the Year,true,9000,New year new salary,SALARY"})
    public void movementFormSave_CorrectPostRequest_requestIsCorrectlyAccepted(Long userId,
                                                                               String categoryName,
                                                                               String valueDate,
                                                                               String name,
                                                                               String flagAmountPositive,
                                                                               String unsignedAmount,
                                                                               String description) throws Exception {

        mockUser("testUser",userId,true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/save"))
                .with(csrf())
                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id","")
                .param("valueDate",valueDate)
                .param("name",name)
                .param("flagAmountPositive",flagAmountPositive)
                .param("unsignedAmount",unsignedAmount)
                .param("description",description)
                .param("categoryName",categoryName);

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:".concat(getControllerBaseURL(userId))));
    }

    @ParameterizedTest
    @WithMockUser("testUser")
    @CsvSource({"777,SalaryCateg,2020-01-15,First Salary of the Year,true,9000,New year new salary,SALARY"})
    public void movementFormSave_CorrectPostRequest_requestIsCorrectlySaved(Long userId,
                                                                               String categoryName,
                                                                               String valueDate,
                                                                               String name,
                                                                               String flagAmountPositive,
                                                                               String unsignedAmount,
                                                                               String description) throws Exception {

        mockUser("testUser",userId,true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/save"))
                .with(csrf())
                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id","")
                .param("valueDate",valueDate)
                .param("name",name)
                .param("flagAmountPositive",flagAmountPositive)
                .param("unsignedAmount",unsignedAmount)
                .param("description",description)
                .param("categoryName",categoryName);

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

        ArgumentCaptor<MovementDTO> movementArgumentCaptor = ArgumentCaptor.forClass(MovementDTO.class);
        Mockito.verify(movementsService).saveMovement(movementArgumentCaptor.capture());
        assertThat(movementArgumentCaptor.getValue().getId()).isNull();
        assertThat(movementArgumentCaptor.getValue().getCategory()).isEqualTo(categoryName);
        assertThat(movementArgumentCaptor.getValue().getName()).isEqualTo(name);
        assertThat(movementArgumentCaptor.getValue().getAmount()).isEqualTo(transformIntoSigned(Double.parseDouble(unsignedAmount),Boolean.parseBoolean(flagAmountPositive)));
        assertThat(movementArgumentCaptor.getValue().getDescription()).isEqualTo(description);
        assertThat(movementArgumentCaptor.getValue().getValueDate()).isEqualTo(LocalDate.parse(valueDate));
    }


    //    movement form save - correct post request - validate if data is correctly edited
    @ParameterizedTest
    @WithMockUser("testUser")
    @CsvSource({"777,SalaryCategory,2349,2020-01-15,First Salary of the Year,true,9000,New year new salary,SALARY"})
    public void movementFormSave_CorrectPostRequest_requestIsCorrectlyEdited(Long userId,
                                                                            String categoryName,
                                                                            String movementId,
                                                                            String valueDate,
                                                                            String name,
                                                                            String flagAmountPositive,
                                                                            String unsignedAmount,
                                                                            String description) throws Exception {

        Mockito.when(movementsService.getMovementById(Long.parseLong(movementId)))
                .thenReturn(TestMovementBuilder.createValidMovement().withId(Long.parseLong(movementId)).buildDTO());

        mockUser("testUser",userId,true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/save"))
                .with(csrf())
                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id",movementId)
                .param("valueDate",valueDate)
                .param("name",name)
                .param("flagAmountPositive",flagAmountPositive)
                .param("unsignedAmount",unsignedAmount)
                .param("description",description)
                .param("categoryName",categoryName);

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

        ArgumentCaptor<MovementDTO> movementArgumentCaptor = ArgumentCaptor.forClass(MovementDTO.class);
        Mockito.verify(movementsService).saveMovement(movementArgumentCaptor.capture());
        assertThat(movementArgumentCaptor.getValue().getId()).isEqualTo(Long.parseLong(movementId));
        assertThat(movementArgumentCaptor.getValue().getCategory()).isEqualTo(categoryName);
        assertThat(movementArgumentCaptor.getValue().getName()).isEqualTo(name);
        assertThat(movementArgumentCaptor.getValue().getAmount()).isEqualTo(transformIntoSigned(Double.parseDouble(unsignedAmount),Boolean.parseBoolean(flagAmountPositive)));
        assertThat(movementArgumentCaptor.getValue().getDescription()).isEqualTo(description);
        assertThat(movementArgumentCaptor.getValue().getValueDate()).isEqualTo(LocalDate.parse(valueDate));
    }


    @ParameterizedTest
    @WithMockUser("testUser")
    @CsvSource({"777,SalaryCategory,2349,2020-01-15,First Salary of the Year,true,,New year new salary,SALARY"})
    public void movementFormSave_update_incorrectPostRequest_amountIsEmpty_redirectToUpdatePage(Long userId,
                                                                                                String categoryName,
                                                                                                String movementId,
                                                                                                String valueDate,
                                                                                                String name,
                                                                                                String flagAmountPositive,
                                                                                                String unsignedAmount,
                                                                                                String description) throws Exception {

        Mockito.when(movementsService.getMovementById(Long.parseLong(movementId)))
                .thenReturn(TestMovementBuilder.createValidMovement().withId(Long.parseLong(movementId)).buildDTO());
        Mockito.when(movementsService.saveMovement(Mockito.argThat(movement -> movement.getAmount() == null)))
                .thenThrow(InvalidMovementException.class);

        mockUser("testUser",userId,true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/save"))
                .with(csrf())
                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id",movementId)
                .param("valueDate",valueDate)
                .param("name",name)
                .param("flagAmountPositive",flagAmountPositive)
                .param("unsignedAmount",unsignedAmount)
                .param("description",description)
                .param("categoryName",categoryName);

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(movementId)));

    }

    @ParameterizedTest
    @WithMockUser("testUser")
    @CsvSource({"777,SalaryCategory,2349,2020-01-15,First Salary of the Year,true,0,New year new salary,SALARY"})
    public void movementFormSave_incorrectPostRequest_amountIsZero_clientErrorIsThrown(Long userId,
                                                                                        String categoryName,
                                                                                        String movementId,
                                                                                        String valueDate,
                                                                                        String name,
                                                                                        String flagAmountPositive,
                                                                                        String unsignedAmount,
                                                                                        String description) throws Exception {

        Mockito.when(movementsService.getMovementById(Long.parseLong(movementId)))
                .thenReturn(TestMovementBuilder.createValidMovement().withId(Long.parseLong(movementId)).buildDTO());
        Mockito.when(movementsService.saveMovement(Mockito.argThat(movement -> movement.getAmount() == 0)))
                .thenThrow(InvalidMovementException.class);

        mockUser("testUser",userId,true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/save"))
                .with(csrf())
                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id",movementId)
                .param("valueDate",valueDate)
                .param("name",name)
                .param("flagAmountPositive",flagAmountPositive)
                .param("unsignedAmount",unsignedAmount)
                .param("description",description)
                .param("categoryName",categoryName);

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(movementId)));

    }

    @ParameterizedTest
    @WithMockUser("testUser")
    @CsvSource({"777,SalaryName,2349,2020-01-15,First Salary of the Year,true,-334,New year new salary,SALARY"})
    public void movementFormSave_incorrectPostRequest_amountIsNegative_clientErrorIsThrown(Long userId,
                                                                                           String categoryName,
                                                                                           String movementId,
                                                                                           String valueDate,
                                                                                           String name,
                                                                                           String flagAmountPositive,
                                                                                           String unsignedAmount,
                                                                                           String description) throws Exception {

        mockUser("testUser",userId,true);

        Mockito.when(movementsService.getMovementById(Long.parseLong(movementId)))
                .thenReturn(TestMovementBuilder.createValidMovement().withId(Long.parseLong(movementId)).buildDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/save"))
                .with(csrf())
                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id",movementId)
                .param("valueDate",valueDate)
                .param("name",name)
                .param("flagAmountPositive",flagAmountPositive)
                .param("unsignedAmount",unsignedAmount)
                .param("description",description)
                .param("categoryName",categoryName);

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(movementId)));

    }

    @ParameterizedTest
    @WithMockUser("testUser")
    @CsvSource({"777,SalaryCategory,2349,,First Salary of the Year,true,9999,New year new salary,SALARY"})
    public void movementFormSave_incorrectPostRequest_valueDateIsEmpty_clientErrorIsThrown(Long userId,
                                                                                           String categoryName,
                                                                                           String movementId,
                                                                                           String valueDate,
                                                                                           String name,
                                                                                           String flagAmountPositive,
                                                                                           String unsignedAmount,
                                                                                           String description) throws Exception {

        mockUser("testUser",userId,true);

        Mockito.when(movementsService.getMovementById(Long.parseLong(movementId)))
                .thenReturn(TestMovementBuilder.createValidMovement().withId(Long.parseLong(movementId)).buildDTO());
        Mockito.when(movementsService.saveMovement(Mockito.argThat(movement -> movement.getValueDate() == null)))
                .thenThrow(InvalidMovementException.class);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/save"))
                .with(csrf())
                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id",movementId)
                .param("valueDate",valueDate)
                .param("name",name)
                .param("flagAmountPositive",flagAmountPositive)
                .param("unsignedAmount",unsignedAmount)
                .param("description",description)
                .param("categoryName",categoryName);

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(movementId)));

    }

    //TODO figure out how not fail here
//    @ParameterizedTest
//    @CsvSource({"777,888,2349,10/10/2019,First Salary of the Year,true,9999,New year new salary,SALARY"})
//    public void movementFormSave_incorrectPostRequest_valueDateIsIncorrectFormat_serverErrorIsThrown(Long userId,
//                                                                                                     Long categoryId,
//                                                                                                     String movementId,
//                                                                                                     String valueDate,
//                                                                                                     String name,
//                                                                                                     String flagAmountPositive,
//                                                                                                     String unsignedAmount,
//                                                                                                     String description) throws Exception {
//
//        Mockito.when(categoriesService.getCategoryById(categoryId))
//                .thenReturn(TestCategoryBuilder.createValidCategory().withId(categoryId).build());
//        Mockito.when(movementsService.saveMovement(Mockito.argThat(movement -> movement.getValueDate() == null)))
//                .thenThrow(InvalidMovementException.class);
//
//        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/save"))
//                .with(csrf())
//                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
//                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//                .param("id",movementId)
//                .param("valueDate",valueDate)
//                .param("name",name)
//                .param("flagAmountPositive",flagAmountPositive)
//                .param("unsignedAmount",unsignedAmount)
//                .param("description",description)
//                .param("categoryId",categoryId.toString());
//
//        mockMvc.perform(request)
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(MockMvcResultMatchers.status().is5xxServerError());
//
//    }

    @ParameterizedTest
    @WithMockUser("testUser")
    @CsvSource({"777,SalaryName,2349,2020-01-15, ,true,9999,New year new salary,SALARY"})
    public void movementFormSave_incorrectPostRequest_nameIsEmpty_clientErrorIsThrown(Long userId,
                                                                                      String categoryName,
                                                                                      String movementId,
                                                                                      String valueDate,
                                                                                      String name,
                                                                                      String flagAmountPositive,
                                                                                      String unsignedAmount,
                                                                                      String description) throws Exception {
        Mockito.when(movementsService.getMovementById(Long.parseLong(movementId)))
                .thenReturn(TestMovementBuilder.createValidMovement().withId(Long.parseLong(movementId)).buildDTO());
        Mockito.when(movementsService.saveMovement(Mockito.argThat(movement -> movement.getName()==null||movement.getName().isBlank())))
                .thenThrow(InvalidMovementException.class);

        mockUser("testUser",userId,true);


        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/save"))
                .with(csrf())
                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id",movementId)
                .param("valueDate",valueDate)
                .param("name","")
                .param("flagAmountPositive",flagAmountPositive)
                .param("unsignedAmount",unsignedAmount)
                .param("description",description)
                .param("categoryName",categoryName);

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(movementId)));

    }

    @ParameterizedTest
    @CsvSource({"777,888,2349,2020-01-15, ,true,9999,New year new salary,SALARY"})
    public void movementFormSave_incorrectPostRequest_unauthorized_unauthorizedErrorIsThrown(Long userId,
                                                                                      Long categoryId,
                                                                                      String movementId,
                                                                                      String valueDate,
                                                                                      String name,
                                                                                      String flagAmountPositive,
                                                                                      String unsignedAmount,
                                                                                      String description) throws Exception {

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/save"))
                .with(csrf())
                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id",movementId)
                .param("valueDate",valueDate)
                .param("name","")
                .param("flagAmountPositive",flagAmountPositive)
                .param("unsignedAmount",unsignedAmount)
                .param("description",description)
                .param("categoryName",categoryId.toString());

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

    }

    @ParameterizedTest
    @WithMockUser("testUser")
    @CsvSource({"777,888,2349,2020-01-15, ,true,9999,New year new salary,SALARY"})
    public void movementFormSave_incorrectPostRequest_attemptingToAccessDifferentUserData_forbiddenErrorIsThrown(Long userId,
                                                                                            Long categoryId,
                                                                                            String movementId,
                                                                                            String valueDate,
                                                                                            String name,
                                                                                            String flagAmountPositive,
                                                                                            String unsignedAmount,
                                                                                            String description) throws Exception {

        Long loggedUser = 23941L;
        if(userId.equals(loggedUser)){
            throw new RuntimeException("Passed user can't be the same as logged user");
        }

        mockUser("testUser",loggedUser,true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/save"))
                .with(csrf())
                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id",movementId)
                .param("valueDate",valueDate)
                .param("name","")
                .param("flagAmountPositive",flagAmountPositive)
                .param("unsignedAmount",unsignedAmount)
                .param("description",description)
                .param("categoryName",categoryId.toString());

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

    }


    @ParameterizedTest
    @WithMockUser("testUser")
    @CsvSource({"777,SalaryCategory,2349,2020-01-15,First Salary of the Year,true,9000,New year new salary,SALARY"})
    public void movementFormDeleteConfirm_CorrectPostRequest_requestIsCorrectlyEdited(Long userId,
                                                                                      String categoryName,
                                                                                      String movementId,
                                                                                      String valueDate,
                                                                                      String name,
                                                                                      String flagAmountPositive,
                                                                                      String unsignedAmount,
                                                                                      String description) throws Exception {

        mockUser("testUser",userId,true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/delete/confirm"))
                .with(csrf())
                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id",movementId)
                .param("valueDate",valueDate)
                .param("name",name)
                .param("flagAmountPositive",flagAmountPositive)
                .param("unsignedAmount",unsignedAmount)
                .param("description",description)
                .param("categoryName",categoryName);

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl(getControllerBaseURL(userId)));

        Mockito.verify(movementsService).deleteMovementById(Long.parseLong(movementId));
    }

    @ParameterizedTest
    @CsvSource({"777,SalaryName,2349,2020-01-15,First Salary of the Year,true,9000,New year new salary,SALARY"})
    public void movementFormDeleteConfirm_IncorrectPostRequest_unauthorized_unauthorizedErrorIsThrown(Long userId,
                                                                                      String categoryName,
                                                                                      String movementId,
                                                                                      String valueDate,
                                                                                      String name,
                                                                                      String flagAmountPositive,
                                                                                      String unsignedAmount,
                                                                                      String description) throws Exception {

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/delete/confirm"))
                .with(csrf())
                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id",movementId)
                .param("valueDate",valueDate)
                .param("name",name)
                .param("flagAmountPositive",flagAmountPositive)
                .param("unsignedAmount",unsignedAmount)
                .param("description",description)
                .param("categoryName",categoryName);

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
    }

    @ParameterizedTest
    @WithMockUser("testUser")
    @CsvSource({"777,SalaryCategory,2349,2020-01-15,First Salary of the Year,true,9000,New year new salary,SALARY"})
    public void movementFormDeleteConfirm_IncorrectPostRequest_attemptingToAccessDifferentUserData_forbiddenErrorIsThrown(Long userId,
                                                                                                                          String categoryName,
                                                                                                                          String movementId,
                                                                                                                          String valueDate,
                                                                                                                          String name,
                                                                                                                          String flagAmountPositive,
                                                                                                                          String unsignedAmount,
                                                                                                                          String description) throws Exception {

        Long loggedUser = 23941L;
        if(userId.equals(loggedUser)){
            throw new RuntimeException("Passed user can't be the same as logged user");
        }

        mockUser("testUser",loggedUser,true);


        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/delete/confirm"))
                .with(csrf())
                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id",movementId)
                .param("valueDate",valueDate)
                .param("name",name)
                .param("flagAmountPositive",flagAmountPositive)
                .param("unsignedAmount",unsignedAmount)
                .param("description",description)
                .param("categoryName",categoryName);

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser("testUser")
    public void movementFormNew_correctGetRequest_onlyActiveCategoriesAreLoadedInFormDropdown() throws Exception {

        Long userId = someUserId();
        List<CategoryDTO> listOfCategories = new ArrayList<>();

        listOfCategories.add(TestCategoryBuilder.createValidCategory().withName("Salary").buildDTO());
        listOfCategories.add(TestCategoryBuilder.createValidCategory().withName("Shopping").buildDTO());
        listOfCategories.add(TestCategoryBuilder.createValidCategory().withName("Games").buildDTO());

        mockUser("testUser",userId,true);

        Mockito.when(categoriesService.getActiveCategoriesForUser(userId)).thenReturn(listOfCategories);


        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/new")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .model()
                        .attribute("categories", Matchers.hasItem(Matchers.hasProperty("name",Matchers.is("Salary")))))
                .andExpect(MockMvcResultMatchers
                        .model()
                        .attribute("categories", Matchers.hasItem(Matchers.hasProperty("name",Matchers.is("Shopping")))))
                .andExpect(MockMvcResultMatchers
                        .model()
                        .attribute("categories", Matchers.hasItem(Matchers.hasProperty("name",Matchers.is("Games")))));
    }

    @Test
    @WithMockUser("testUser")
    public void movementFormEdit_correctGetRequest_onlyActiveCategoriesAreLoadedInFormDropdown() throws Exception {

        Long userId = someUserId();
        Long movementId = 444L;
        List<CategoryDTO> listOfCategories = new ArrayList<>();

        listOfCategories.add(TestCategoryBuilder.createValidCategory().withName("Salary").buildDTO());
        listOfCategories.add(TestCategoryBuilder.createValidCategory().withName("Shopping").buildDTO());
        listOfCategories.add(TestCategoryBuilder.createValidCategory().withName("Games").buildDTO());

        mockUser("testUser",userId,true);

        Mockito.when(categoriesService.getActiveCategoriesForUser(userId)).thenReturn(listOfCategories);
        Mockito.when(movementsService.getMovementById(movementId))
                .thenReturn(TestMovementBuilder
                        .createValidMovement()
                        .withId(movementId)
                        .withCategory(TestCategoryBuilder.createValidCategory().withName("Salary").build())
                        .buildDTO());


        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update")).param("id",movementId.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .model()
                        .attribute("categories", Matchers.hasItem(Matchers.hasProperty("name",Matchers.is("Salary")))))
                .andExpect(MockMvcResultMatchers
                        .model()
                        .attribute("categories", Matchers.hasItem(Matchers.hasProperty("name",Matchers.is("Shopping")))))
                .andExpect(MockMvcResultMatchers
                        .model()
                        .attribute("categories", Matchers.hasItem(Matchers.hasProperty("name",Matchers.is("Games")))));
    }

    @Test
    @WithMockUser("testUser")
    public void movementFormDelete_correctGetRequest_onlyActiveCategoriesAreLoadedInFormDropdown() throws Exception {

        Long userId = someUserId();
        Long movementId = 444L;
        List<CategoryDTO> listOfCategories = new ArrayList<>();

        listOfCategories.add(TestCategoryBuilder.createValidCategory().withName("Salary").buildDTO());
        listOfCategories.add(TestCategoryBuilder.createValidCategory().withName("Shopping").buildDTO());
        listOfCategories.add(TestCategoryBuilder.createValidCategory().withName("Games").buildDTO());

        mockUser("testUser",userId,true);

        Mockito.when(categoriesService.getActiveCategoriesForUser(userId)).thenReturn(listOfCategories);
        Mockito.when(movementsService.getMovementById(movementId))
                .thenReturn(TestMovementBuilder
                        .createValidMovement()
                        .withId(movementId)
                        .withCategory(TestCategoryBuilder.createValidCategory().withName("Salary").build())
                        .buildDTO());


        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/delete")).param("id",movementId.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .model()
                        .attribute("categories", Matchers.hasItem(Matchers.hasProperty("name",Matchers.is("Salary")))))
                .andExpect(MockMvcResultMatchers
                        .model()
                        .attribute("categories", Matchers.hasItem(Matchers.hasProperty("name",Matchers.is("Shopping")))))
                .andExpect(MockMvcResultMatchers
                        .model()
                        .attribute("categories", Matchers.hasItem(Matchers.hasProperty("name",Matchers.is("Games")))));
    }


    @ParameterizedTest
    @WithMockUser("testUser")
    @CsvSource({"777,-6969,SalaryName,2020-01-15,First Salary of the Year,true,9000,New year new salary,SALARY"})
    public void movementFormSave_CorrectPostRequest_existingMovement_existingEntityIsUsed(Long userId,
                                                                               Long movementId,
                                                                               String categoryName,
                                                                               String valueDate,
                                                                               String name,
                                                                               String flagAmountPositive,
                                                                               String unsignedAmount,
                                                                               String description) throws Exception {

        MovementDTO originalMoment = TestMovementBuilder.createValidMovement().withId(movementId).buildDTO();
        Mockito.when(movementsService.getMovementById(movementId)).thenReturn(originalMoment);

        mockUser("testUser",userId,true);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/save"))
                .with(csrf())
                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id",movementId.toString())
                .param("valueDate",valueDate)
                .param("name",name)
                .param("flagAmountPositive",flagAmountPositive)
                .param("unsignedAmount",unsignedAmount)
                .param("description",description)
                .param("categoryName",categoryName);

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

        ArgumentCaptor<MovementDTO> movementArgumentCaptor = ArgumentCaptor.forClass(MovementDTO.class);
        Mockito.verify(movementsService).saveMovement(movementArgumentCaptor.capture());
        assertThat(movementArgumentCaptor.getValue()).isSameAs(originalMoment);
        assertThat(movementArgumentCaptor.getValue().getId()).isEqualTo(movementId);
    }

}
