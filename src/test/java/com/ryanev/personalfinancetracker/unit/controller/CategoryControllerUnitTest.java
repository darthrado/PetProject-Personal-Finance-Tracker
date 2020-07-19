package com.ryanev.personalfinancetracker.unit.controller;

import com.ryanev.personalfinancetracker.controllers.CategoriesController;
import com.ryanev.personalfinancetracker.entities.MovementCategory;
import com.ryanev.personalfinancetracker.exceptions.InvalidCategoryException;
import com.ryanev.personalfinancetracker.services.CategoriesService;
import com.ryanev.personalfinancetracker.services.UserService;
import com.ryanev.personalfinancetracker.util.TestCategoryBuilder;
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

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CategoriesController.class)
public class CategoryControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @MockBean
    private CategoriesService categoriesService;

    private String getControllerBaseURL(Long userId){
        return "/"+userId+"/categories";
    }
    private Long someUserId(){
        return 888777666L;
    }

    @Test
    public void categoriesPage_CorrectGetRequest_allCategoriesForUserArePresent() throws Exception{
        Long userId = 777L;
        Integer expectedRecords = 8;

        Mockito.when(categoriesService.getCategoriesForUser(userId))
                .thenReturn(Collections.nCopies(expectedRecords, TestCategoryBuilder.createValidCategory().build()));

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("categoriesList", Matchers.iterableWithSize(expectedRecords)));
    }

    @ParameterizedTest
    @CsvSource({"234,SALARY", "666,OTHER", "543,Games"})
    public void categoriesPage_correctGetRequest_dataOnASingleCategoryIsCorrect(Long userId,
                                                                               String movementCategoryName) throws Exception{

        MovementCategory movementCategory = TestCategoryBuilder.createValidCategory().withName(movementCategoryName).build();
        Mockito.when(categoriesService.getCategoriesForUser(userId)).thenReturn(List.of(movementCategory));

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model()
                        .attribute("categoriesList",Matchers
                                .hasItem(Matchers.allOf(
                                        Matchers.hasProperty("name",Matchers.is(movementCategoryName))
                                ))
                        ));
    }

    @Test
    public void categoriesPage_correctGetRequest_newCategoryButtonIsPresent() throws Exception{
        Long userId = someUserId();

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .string(Matchers.containsString(getControllerBaseURL(userId).concat("/new"))));
    }

    @Test
    public void categoriesPage_correctGetRequest_editAndDeleteButtonsArePresent() throws Exception{

        Long userId = someUserId();
        Integer expectedRecords = 8;

        Mockito.when(categoriesService.getCategoriesForUser(userId))
                .thenReturn(Collections.nCopies(expectedRecords, TestCategoryBuilder.createValidCategory().build()));

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(getControllerBaseURL(userId)+"/update")))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(getControllerBaseURL(userId)+"/delete")));
    }


    @ParameterizedTest
    @ValueSource(longs = {-22,3456,77,22222,1234567456})
    public void categoriesPage_incorrectGetRequest_incorrectUserId_clientErrorIsThrown(Long userId) throws Exception{

        Mockito.when(userService.getUserById(userId)).thenThrow(NoSuchElementException.class);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @ParameterizedTest
    @ValueSource(strings = {"id","name","description"})
    public void categoryFormNew_correctGetRequest_allFormFieldsArePresent(String fieldId) throws Exception{

        Long userId = someUserId();
        String testString = "id=\""+fieldId+"\"";

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/new")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(testString)));
    }

    @Test
    public void categoryFormNew_correctGetRequest_saveLinkIsPresent() throws Exception{
        Long userId = someUserId();

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/new")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(getControllerBaseURL(userId).concat("/save"))));
    }

    @Test
    public void categoryFormNew_correctGetRequest_backLinkIsPresent() throws Exception{
        Long userId = someUserId();

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/new")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(getControllerBaseURL(userId))));
    }

    @ParameterizedTest
    @ValueSource(longs = {-22,3456,77,22222,1234567456})
    public void categoryFormNew_incorrectGetRequest_incorrectUserId_clientErrorIsThrown(Long userId) throws Exception{

        Mockito.when(userService.getUserById(userId)).thenThrow(NoSuchElementException.class);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/new")))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @ParameterizedTest
    @CsvSource({"-333,1,SALARY"})
    public void categoryFormEdit_correctGetRequest_formDataIsCorrectlyLoaded(Long userId,
                                                                             Long categoryId,
                                                                             String categoryName) throws Exception {

        MovementCategory categoryToEdit = TestCategoryBuilder.createValidCategory()
                .withId(categoryId)
                .withName(categoryName)
                .build();

        Mockito.when(categoriesService.getCategoryById(categoryId)).thenReturn(categoryToEdit);


        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(categoryId.toString())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers
                        .model()
                        .attribute("category", Matchers.allOf(
                                Matchers.hasProperty("id",Matchers.is(categoryId)),
                                Matchers.hasProperty("name",Matchers.is(categoryName))))
                );
    }

    @Test
    public void movementFormEdit_correctGetRequest_saveLinkIsPresent() throws Exception{
        Long userId = someUserId();
        Long categoryId = -112233L;


        MovementCategory movementCategory = TestCategoryBuilder.createValidCategory().withId(categoryId).build();

        Mockito.when(categoriesService.getCategoryById(categoryId)).thenReturn(movementCategory);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(categoryId.toString())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(getControllerBaseURL(userId).concat("/save"))));
    }

    @ParameterizedTest
    @CsvSource({"42,1","-777,2","666,3"})
    public void categoryFormEdit_incorrectGetRequest_incorrectUserId_clientErrorIsThrown(Long userId, Long categoryId) throws Exception{
        Mockito.when(userService.getUserById(userId)).thenThrow(NoSuchElementException.class);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(categoryId.toString())))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    public void categoryFormEdit_incorrectGetRequest_missingCategoryId_clientErrorIsThrown() throws Exception{
        Long userId = someUserId();

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update")))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    public void movementFormEdit_incorrectGetRequest_incorrectCategoryId_clientErrorIsThrown() throws Exception{
        Long userId = someUserId();
        Long categoryId = -112233L;

        Mockito.when(categoriesService.getCategoryById(categoryId)).thenThrow(NoSuchElementException.class);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(categoryId.toString())))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @ParameterizedTest
    @CsvSource({"777,SALARY"})
    public void categoryFormSave_CorrectPostRequest_requestIsCorrectlySaved(Long userId,
                                                                            String name) throws Exception {

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/save"))
                .with(csrf())
                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id","")
                .param("name",name);

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

        ArgumentCaptor<MovementCategory> categoryArgumentCaptorArgumentCaptor = ArgumentCaptor.forClass(MovementCategory.class);
        Mockito.verify(categoriesService).saveCategory(categoryArgumentCaptorArgumentCaptor.capture());
        assertThat(categoryArgumentCaptorArgumentCaptor.getValue().getId()).isNull();
        assertThat(categoryArgumentCaptorArgumentCaptor.getValue().getName()).isEqualTo(name);
    }

    @Test
    public void categoryFormSave_incorrectPostRequest_nameIsEmpty_clientErrorIsThrown() throws Exception {

        Long userId = someUserId();

        Mockito.when(categoriesService.saveCategory(Mockito.argThat(category -> category.getName()==null||category.getName().isBlank())))
                .thenThrow(InvalidCategoryException.class);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/save"))
                .with(csrf())
                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name","");

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

    }

    @ParameterizedTest
    @CsvSource({"777,SALARY"})
    public void categoryFormSave_incorrectPostRequest_userIdIsIncorrect_clientErrorIsThrown(Long userId,
                                                                                            String name) throws Exception {

        Mockito.when(userService.getUserById(userId))
                .thenThrow(NoSuchElementException.class);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/save"))
                .with(csrf())
                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name",name);

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());

    }


}
