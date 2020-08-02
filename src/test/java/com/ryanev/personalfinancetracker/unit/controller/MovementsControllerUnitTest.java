package com.ryanev.personalfinancetracker.unit.controller;


import com.ryanev.personalfinancetracker.controllers.MovementsController;
import com.ryanev.personalfinancetracker.entities.Movement;
import com.ryanev.personalfinancetracker.entities.MovementCategory;
import com.ryanev.personalfinancetracker.exceptions.InvalidMovementException;
import com.ryanev.personalfinancetracker.services.CategoriesService;
import com.ryanev.personalfinancetracker.services.MovementsService;
import com.ryanev.personalfinancetracker.services.UserService;
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

    @Test
    public void movementsPage_CorrectGetRequest_allMovementsForUserArePresent() throws Exception{
        Long userId = 777L;
        Integer expectedRecords = 8;

        Mockito.when(movementsService.getMovementsForUser(userId))
                .thenReturn(Collections.nCopies(expectedRecords, TestMovementBuilder.createValidMovement().build()));

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("movementsList", Matchers.iterableWithSize(expectedRecords)));
    }

    @ParameterizedTest
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
        Movement existingMovement = TestMovementBuilder.createValidMovement()
                .withName(movementName)
                .withAmount(movementAmount)
                .withDate(LocalDate.of(movementYear,movementMonth,movementDay))
                .withCategory(movementCategory)
                .build();
        Mockito.when(movementsService.getMovementsForUser(userId)).thenReturn(List.of(existingMovement));

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
    public void movementsPage_correctGetRequest_newMovementButtonIsPresent() throws Exception{
        Long userId = someUserId();

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .string(Matchers.containsString(getControllerBaseURL(userId).concat("/new"))));
    }

    @Test
    //TODO: we expect each movement to have Edit and Delete links. see how
    public void movementsPage_correctGetRequest_editAndDeleteButtonsArePresent() throws Exception{

        Long userId = someUserId();
        Integer expectedRecords = 8;

        Mockito.when(movementsService.getMovementsForUser(userId))
                .thenReturn(Collections.nCopies(expectedRecords, TestMovementBuilder.createValidMovement().build()));

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(getControllerBaseURL(userId)+"/update")))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(getControllerBaseURL(userId)+"/delete")));
    }

    @ParameterizedTest
    @ValueSource(longs = {-22,3456,77,22222,1234567456})
    public void movementsPage_incorrectGetRequest_incorrectUserId_clientErrorIsThrown(Long userId) throws Exception{

        Mockito.when(userService.getUserById(userId)).thenThrow(NoSuchElementException.class);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }


    @ParameterizedTest
    @ValueSource(strings = {"id","valueDate","flagAmountPositive","unsignedAmount","name","description","categoryId"})
    //TODO there is probably a better way to do this - check all fields without performing to many get requests
    public void movementFormNew_correctGetRequest_allFormFieldsArePresent(String fieldId) throws Exception{

        Long userId = someUserId();
        String testString = "id=\""+fieldId+"\"";

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/new")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(testString)));
    }


    @Test
    public void movementFormNew_correctGetRequest_saveLinkIsPresent() throws Exception{
        Long userId = someUserId();

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/new")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(getControllerBaseURL(userId).concat("/save"))));
    }

    @Test
    public void movementFormNew_correctGetRequest_backLinkIsPresent() throws Exception{
        Long userId = someUserId();

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/new")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(getControllerBaseURL(userId))));
    }

    @ParameterizedTest
    @ValueSource(longs = {-22,3456,77,22222,1234567456})
    public void movementFormNew_incorrectGetRequest_incorrectUserId_clientErrorIsThrown(Long userId) throws Exception{

        Mockito.when(userService.getUserById(userId)).thenThrow(NoSuchElementException.class);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/new")))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @ParameterizedTest
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

        MovementCategory movementCategory = TestCategoryBuilder.createValidCategory()
                .withId(categoryId)
                .withName(movementCategoryName)
                .build();

        Movement movementToEdit = TestMovementBuilder.createValidMovement()
                .withId(movementId)
                .withDate(movementDate)
                .withName(movementName)
                .withAmount(transformIntoSigned(movementUnsignedAmount,movementSignPositive))
                .withCategory(movementCategory)
                .withDescription(movementDescription)
                .build();

        Mockito.when(movementsService.getMovementById(movementId)).thenReturn(movementToEdit);
        Mockito.when(categoriesService.getActiveCategoriesForUser(userId)).thenReturn(List.of(movementCategory));


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
                                Matchers.hasProperty("categoryId",Matchers.is(categoryId)),
                                Matchers.hasProperty("description",Matchers.is(movementDescription))))
                );
    }

    @Test
    public void movementFormEdit_correctGetRequest_saveLinkIsPresent() throws Exception{
        Long userId = someUserId();
        Long movementId = -112233L;


        MovementCategory movementCategory = TestCategoryBuilder.createValidCategory().build();

        Movement movementToEdit = TestMovementBuilder.createValidMovement()
                .withId(movementId)
                .build();

        Mockito.when(movementsService.getMovementById(movementId)).thenReturn(movementToEdit);
        Mockito.when(categoriesService.getActiveCategoriesForUser(userId)).thenReturn(List.of(movementCategory));

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(movementId.toString())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(getControllerBaseURL(userId).concat("/save"))));
    }

    @ParameterizedTest
    @CsvSource({"42,1","-777,2","666,3"})
    public void movementFormEdit_incorrectGetRequest_incorrectUserId_clientErrorIsThrown(Long userId, Long movementId) throws Exception{
        Mockito.when(userService.getUserById(userId)).thenThrow(NoSuchElementException.class);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(movementId.toString())))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    public void movementFormEdit_incorrectGetRequest_missingMovementId_clientErrorIsThrown() throws Exception{
        Long userId = someUserId();

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update")))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    public void movementFormEdit_incorrectGetRequest_incorrectMovementId_clientErrorIsThrown() throws Exception{
            Long userId = someUserId();
            Long movementId = -112233L;

            Mockito.when(movementsService.getMovementById(movementId)).thenThrow(NoSuchElementException.class);

            mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(movementId.toString())))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @ParameterizedTest
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

        MovementCategory movementCategory = TestCategoryBuilder.createValidCategory()
                .withId(categoryId)
                .withName(movementCategoryName)
                .build();

        Movement movementToEdit = TestMovementBuilder.createValidMovement()
                .withId(movementId)
                .withDate(movementDate)
                .withName(movementName)
                .withAmount(transformIntoSigned(movementUnsignedAmount,movementSignPositive))
                .withCategory(movementCategory)
                .withDescription(movementDescription)
                .build();

        Mockito.when(movementsService.getMovementById(movementId)).thenReturn(movementToEdit);
        Mockito.when(categoriesService.getActiveCategoriesForUser(userId)).thenReturn(List.of(movementCategory));
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
                                Matchers.hasProperty("categoryId",Matchers.is(categoryId)),
                                Matchers.hasProperty("description",Matchers.is(movementDescription))))
                );
    }

    @Test
    public void movementFormDelete_correctGetRequest_allDataIsCorrectlyDisabled() throws Exception {
        Long userId = someUserId();
        Long movementId = -112233L;


        MovementCategory movementCategory = TestCategoryBuilder.createValidCategory().build();

        Movement movementToEdit = TestMovementBuilder.createValidMovement()
                .withId(movementId)
                .build();

        Mockito.when(movementsService.getMovementById(movementId)).thenReturn(movementToEdit);
        Mockito.when(categoriesService.getActiveCategoriesForUser(userId)).thenReturn(List.of(movementCategory));

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/delete").concat("?id=").concat(movementId.toString())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .model()
                        .attribute("disableFormFields",Matchers.is(true)));
    }

    @Test
    public void movementFormDelete_correctGetRequest_confirmDeleteLinkIsPresent() throws Exception{
        Long userId = someUserId();
        Long movementId = -112233L;


        MovementCategory movementCategory = TestCategoryBuilder.createValidCategory().build();

        Movement movementToEdit = TestMovementBuilder.createValidMovement()
                .withId(movementId)
                .build();

        Mockito.when(movementsService.getMovementById(movementId)).thenReturn(movementToEdit);
        Mockito.when(categoriesService.getActiveCategoriesForUser(userId)).thenReturn(List.of(movementCategory));

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/delete").concat("?id=").concat(movementId.toString())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .content().string(Matchers.containsString(getControllerBaseURL(userId).concat("/delete/confirm"))));
    }


    @ParameterizedTest
    @CsvSource({"42,1","-777,2","666,3"})
    public void movementFormDelete_incorrectGetRequest_incorrectUserId_clientErrorIsThrown(Long userId, Long movementId) throws Exception {

        Mockito.when(userService.getUserById(userId)).thenThrow(NoSuchElementException.class);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/delete").concat("?id=").concat(movementId.toString())))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    public void movementFormDelete_incorrectGetRequest_missingMovementId_clientErrorIsThrown() throws Exception{
        Long userId = someUserId();

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/delete")))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    public void movementFormDelete_incorrectGetRequest_incorrectMovementId_clientErrorIsThrown() throws Exception{
        Long userId = someUserId();
        Long movementId = -1337L;
        Mockito.when(movementsService.getMovementById(movementId)).thenThrow(NoSuchElementException.class);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/delete").concat("?id=").concat(movementId.toString())))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @ParameterizedTest
    @CsvSource({"777,888,2020-01-15,First Salary of the Year,true,9000,New year new salary,SALARY"})
    public void movementFormSave_CorrectPostRequest_requestIsCorrectlyAccepted(Long userId,
                                                                               Long categoryId,
                                                                               String valueDate,
                                                                               String name,
                                                                               String flagAmountPositive,
                                                                               String unsignedAmount,
                                                                               String description) throws Exception {

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
                .param("categoryId",categoryId.toString());

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.view().name("redirect:".concat(getControllerBaseURL(userId))));
    }

    @ParameterizedTest
    @CsvSource({"777,888,2020-01-15,First Salary of the Year,true,9000,New year new salary,SALARY"})
    public void movementFormSave_CorrectPostRequest_requestIsCorrectlySaved(Long userId,
                                                                               Long categoryId,
                                                                               String valueDate,
                                                                               String name,
                                                                               String flagAmountPositive,
                                                                               String unsignedAmount,
                                                                               String description) throws Exception {

        Mockito.when(categoriesService.getCategoryById(categoryId))
                .thenReturn(TestCategoryBuilder.createValidCategory().withId(categoryId).build());

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
                .param("categoryId",categoryId.toString());

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

        ArgumentCaptor<Movement> movementArgumentCaptor = ArgumentCaptor.forClass(Movement.class);
        Mockito.verify(movementsService).saveMovement(movementArgumentCaptor.capture());
        assertThat(movementArgumentCaptor.getValue().getId()).isNull();
        assertThat(movementArgumentCaptor.getValue().getCategory().getId()).isEqualTo(categoryId);
        assertThat(movementArgumentCaptor.getValue().getName()).isEqualTo(name);
        assertThat(movementArgumentCaptor.getValue().getAmount()).isEqualTo(transformIntoSigned(Double.parseDouble(unsignedAmount),Boolean.parseBoolean(flagAmountPositive)));
        assertThat(movementArgumentCaptor.getValue().getDescription()).isEqualTo(description);
        assertThat(movementArgumentCaptor.getValue().getValueDate()).isEqualTo(LocalDate.parse(valueDate));
    }


    //    movement form save - correct post request - validate if data is correctly edited
    @ParameterizedTest
    @CsvSource({"777,888,2349,2020-01-15,First Salary of the Year,true,9000,New year new salary,SALARY"})
    public void movementFormSave_CorrectPostRequest_requestIsCorrectlyEdited(Long userId,
                                                                            Long categoryId,
                                                                            String movementId,
                                                                            String valueDate,
                                                                            String name,
                                                                            String flagAmountPositive,
                                                                            String unsignedAmount,
                                                                            String description) throws Exception {

        Mockito.when(movementsService.getMovementById(Long.parseLong(movementId)))
                .thenReturn(TestMovementBuilder.createValidMovement().withId(Long.parseLong(movementId)).build());
        Mockito.when(categoriesService.getCategoryById(categoryId))
                .thenReturn(TestCategoryBuilder.createValidCategory().withId(categoryId).build());

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
                .param("categoryId",categoryId.toString());

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

        ArgumentCaptor<Movement> movementArgumentCaptor = ArgumentCaptor.forClass(Movement.class);
        Mockito.verify(movementsService).saveMovement(movementArgumentCaptor.capture());
        assertThat(movementArgumentCaptor.getValue().getId()).isEqualTo(Long.parseLong(movementId));
        assertThat(movementArgumentCaptor.getValue().getCategory().getId()).isEqualTo(categoryId);
        assertThat(movementArgumentCaptor.getValue().getName()).isEqualTo(name);
        assertThat(movementArgumentCaptor.getValue().getAmount()).isEqualTo(transformIntoSigned(Double.parseDouble(unsignedAmount),Boolean.parseBoolean(flagAmountPositive)));
        assertThat(movementArgumentCaptor.getValue().getDescription()).isEqualTo(description);
        assertThat(movementArgumentCaptor.getValue().getValueDate()).isEqualTo(LocalDate.parse(valueDate));
    }


    @ParameterizedTest
    @CsvSource({"777,888,2349,2020-01-15,First Salary of the Year,true,,New year new salary,SALARY"})
    public void movementFormSave_incorrectPostRequest_amountIsEmpty_clientErrorIsThrown(Long userId,
                                                                                        Long categoryId,
                                                                                        String movementId,
                                                                                        String valueDate,
                                                                                        String name,
                                                                                        String flagAmountPositive,
                                                                                        String unsignedAmount,
                                                                                        String description) throws Exception {

        Mockito.when(movementsService.getMovementById(Long.parseLong(movementId)))
                .thenReturn(TestMovementBuilder.createValidMovement().withId(Long.parseLong(movementId)).build());
        Mockito.when(categoriesService.getCategoryById(categoryId))
                .thenReturn(TestCategoryBuilder.createValidCategory().withId(categoryId).build());
        Mockito.when(movementsService.saveMovement(Mockito.argThat(movement -> movement.getAmount() == null)))
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
                .param("categoryId",categoryId.toString());

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

    }

    @ParameterizedTest
    @CsvSource({"777,888,2349,2020-01-15,First Salary of the Year,true,0,New year new salary,SALARY"})
    public void movementFormSave_incorrectPostRequest_amountIsZero_clientErrorIsThrown(Long userId,
                                                                                        Long categoryId,
                                                                                        String movementId,
                                                                                        String valueDate,
                                                                                        String name,
                                                                                        String flagAmountPositive,
                                                                                        String unsignedAmount,
                                                                                        String description) throws Exception {

        Mockito.when(movementsService.getMovementById(Long.parseLong(movementId)))
                .thenReturn(TestMovementBuilder.createValidMovement().withId(Long.parseLong(movementId)).build());
        Mockito.when(categoriesService.getCategoryById(categoryId))
                .thenReturn(TestCategoryBuilder.createValidCategory().withId(categoryId).build());
        Mockito.when(movementsService.saveMovement(Mockito.argThat(movement -> movement.getAmount() == 0)))
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
                .param("categoryId",categoryId.toString());

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

    }

    @ParameterizedTest
    @CsvSource({"777,888,2349,2020-01-15,First Salary of the Year,true,-334,New year new salary,SALARY"})
    public void movementFormSave_incorrectPostRequest_amountIsNegative_clientErrorIsThrown(Long userId,
                                                                                           Long categoryId,
                                                                                           String movementId,
                                                                                           String valueDate,
                                                                                           String name,
                                                                                           String flagAmountPositive,
                                                                                           String unsignedAmount,
                                                                                           String description) throws Exception {

        Mockito.when(movementsService.getMovementById(Long.parseLong(movementId)))
                .thenReturn(TestMovementBuilder.createValidMovement().withId(Long.parseLong(movementId)).build());
        Mockito.when(categoriesService.getCategoryById(categoryId))
                .thenReturn(TestCategoryBuilder.createValidCategory().withId(categoryId).build());

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
                .param("categoryId",categoryId.toString());

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

    }

    @ParameterizedTest
    @CsvSource({"777,888,2349,,First Salary of the Year,true,9999,New year new salary,SALARY"})
    public void movementFormSave_incorrectPostRequest_valueDateIsEmpty_clientErrorIsThrown(Long userId,
                                                                                           Long categoryId,
                                                                                           String movementId,
                                                                                           String valueDate,
                                                                                           String name,
                                                                                           String flagAmountPositive,
                                                                                           String unsignedAmount,
                                                                                           String description) throws Exception {

        Mockito.when(movementsService.getMovementById(Long.parseLong(movementId)))
                .thenReturn(TestMovementBuilder.createValidMovement().withId(Long.parseLong(movementId)).build());
        Mockito.when(categoriesService.getCategoryById(categoryId))
                .thenReturn(TestCategoryBuilder.createValidCategory().withId(categoryId).build());
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
                .param("categoryId",categoryId.toString());

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

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
    @CsvSource({"777,888,2349,2020-01-15, ,true,9999,New year new salary,SALARY"})
    public void movementFormSave_incorrectPostRequest_nameIsEmpty_clientErrorIsThrown(Long userId,
                                                                                      Long categoryId,
                                                                                      String movementId,
                                                                                      String valueDate,
                                                                                      String name,
                                                                                      String flagAmountPositive,
                                                                                      String unsignedAmount,
                                                                                      String description) throws Exception {
        Mockito.when(movementsService.getMovementById(Long.parseLong(movementId)))
                .thenReturn(TestMovementBuilder.createValidMovement().withId(Long.parseLong(movementId)).build());
        Mockito.when(categoriesService.getCategoryById(categoryId))
                .thenReturn(TestCategoryBuilder.createValidCategory().withId(categoryId).build());
        Mockito.when(movementsService.saveMovement(Mockito.argThat(movement -> movement.getName()==null||movement.getName().isBlank())))
                .thenThrow(InvalidMovementException.class);

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
                .param("categoryId",categoryId.toString());

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

    }

    @ParameterizedTest
    @CsvSource({"777,888,2349,2020-01-15, ,true,9999,New year new salary,SALARY"})
    public void movementFormSave_incorrectPostRequest_userIdIsIncorrect_clientErrorIsThrown(Long userId,
                                                                                      Long categoryId,
                                                                                      String movementId,
                                                                                      String valueDate,
                                                                                      String name,
                                                                                      String flagAmountPositive,
                                                                                      String unsignedAmount,
                                                                                      String description) throws Exception {

        Mockito.when(userService.getUserById(userId))
                .thenThrow(NoSuchElementException.class);

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
                .param("categoryId",categoryId.toString());

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

    }

    @ParameterizedTest
    @CsvSource({"777,888,2349,2020-01-15, ,true,9999,New year new salary,SALARY"})
    public void movementFormSave_incorrectPostRequest_categoryIdIsIncorrect_clientErrorIsThrown(Long userId,
                                                                                            Long categoryId,
                                                                                            String movementId,
                                                                                            String valueDate,
                                                                                            String name,
                                                                                            String flagAmountPositive,
                                                                                            String unsignedAmount,
                                                                                            String description) throws Exception {


        Mockito.when(movementsService.getMovementById(Long.parseLong(movementId)))
                .thenReturn(TestMovementBuilder.createValidMovement().withId(Long.parseLong(movementId)).build());
        Mockito.when(categoriesService.getCategoryById(categoryId))
                .thenThrow(NoSuchElementException.class);

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
                .param("categoryId",categoryId.toString());

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

    }


    @ParameterizedTest
    @CsvSource({"777,888,2349,2020-01-15,First Salary of the Year,true,9000,New year new salary,SALARY"})
    public void movementFormDeleteConfirm_CorrectPostRequest_requestIsCorrectlyEdited(Long userId,
                                                                                      Long categoryId,
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
                .param("categoryId",categoryId.toString());

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

        Mockito.verify(movementsService).deleteMovementById(Long.parseLong(movementId));
    }

    @ParameterizedTest
    @CsvSource({"777,888,2349,2020-01-15,First Salary of the Year,true,9000,New year new salary,SALARY"})
    public void movementFormDeleteConfirm_IncorrectPostRequest_userIdIsIncorrect_clientErrorIsThrown(Long userId,
                                                                                      Long categoryId,
                                                                                      String movementId,
                                                                                      String valueDate,
                                                                                      String name,
                                                                                      String flagAmountPositive,
                                                                                      String unsignedAmount,
                                                                                      String description) throws Exception {

        Mockito.when(userService.getUserById(userId))
                .thenThrow(NoSuchElementException.class);

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
                .param("categoryId",categoryId.toString());

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    public void movementFormNew_correctGetRequest_onlyActiveCategoriesAreLoadedInFormDropdown() throws Exception {

        Long userId = someUserId();
        List<MovementCategory> listOfCategories = new ArrayList<>();

        listOfCategories.add(TestCategoryBuilder.createValidCategory().withName("Salary").build());
        listOfCategories.add(TestCategoryBuilder.createValidCategory().withName("Shopping").build());
        listOfCategories.add(TestCategoryBuilder.createValidCategory().withName("Games").build());


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
    public void movementFormEdit_correctGetRequest_onlyActiveCategoriesAreLoadedInFormDropdown() throws Exception {

        Long userId = someUserId();
        Long movementId = 444L;
        List<MovementCategory> listOfCategories = new ArrayList<>();

        listOfCategories.add(TestCategoryBuilder.createValidCategory().withName("Salary").build());
        listOfCategories.add(TestCategoryBuilder.createValidCategory().withName("Shopping").build());
        listOfCategories.add(TestCategoryBuilder.createValidCategory().withName("Games").build());


        Mockito.when(categoriesService.getActiveCategoriesForUser(userId)).thenReturn(listOfCategories);
        Mockito.when(movementsService.getMovementById(movementId))
                .thenReturn(TestMovementBuilder.createValidMovement().withId(movementId).withCategory(listOfCategories.get(0)).build());


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
    public void movementFormDelete_correctGetRequest_onlyActiveCategoriesAreLoadedInFormDropdown() throws Exception {

        Long userId = someUserId();
        Long movementId = 444L;
        List<MovementCategory> listOfCategories = new ArrayList<>();

        listOfCategories.add(TestCategoryBuilder.createValidCategory().withName("Salary").build());
        listOfCategories.add(TestCategoryBuilder.createValidCategory().withName("Shopping").build());
        listOfCategories.add(TestCategoryBuilder.createValidCategory().withName("Games").build());


        Mockito.when(categoriesService.getActiveCategoriesForUser(userId)).thenReturn(listOfCategories);
        Mockito.when(movementsService.getMovementById(movementId))
                .thenReturn(TestMovementBuilder.createValidMovement().withId(movementId).withCategory(listOfCategories.get(0)).build());


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
    @CsvSource({"777,-6969,888,2020-01-15,First Salary of the Year,true,9000,New year new salary,SALARY"})
    public void movementFormSave_CorrectPostRequest_existingMovement_existingEntityIsUsed(Long userId,
                                                                               Long movementId,
                                                                               Long categoryId,
                                                                               String valueDate,
                                                                               String name,
                                                                               String flagAmountPositive,
                                                                               String unsignedAmount,
                                                                               String description) throws Exception {

        Movement originalMoment = TestMovementBuilder.createValidMovement().withId(movementId).build();
        Mockito.when(movementsService.getMovementById(movementId)).thenReturn(originalMoment);

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
                .param("categoryId",categoryId.toString());

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

        ArgumentCaptor<Movement> movementArgumentCaptor = ArgumentCaptor.forClass(Movement.class);
        Mockito.verify(movementsService).saveMovement(movementArgumentCaptor.capture());
        assertThat(movementArgumentCaptor.getValue()).isSameAs(originalMoment);
        assertThat(movementArgumentCaptor.getValue().getId()).isEqualTo(movementId);
    }

}
