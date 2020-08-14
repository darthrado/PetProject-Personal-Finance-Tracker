package com.ryanev.personalfinancetracker.unit.controller;

import com.ryanev.personalfinancetracker.web.controllers.CategoriesController;
import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import com.ryanev.personalfinancetracker.exceptions.IncorrectCategoryIdException;
import com.ryanev.personalfinancetracker.exceptions.InvalidCategoryException;
import com.ryanev.personalfinancetracker.services.categories.CategoriesService;
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
import org.springframework.web.util.*;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SuppressWarnings("SameReturnValue")
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
    @CsvSource({"234,SALARY,Active", "666,OTHER,Disabled", "543,Games,Active"})
    public void categoriesPage_correctGetRequest_dataOnASingleCategoryIsCorrect(Long userId,
                                                                                String movementCategoryName,
                                                                                String categoryStatus) throws Exception{

        MovementCategory movementCategory = TestCategoryBuilder
                .createValidCategory()
                .withName(movementCategoryName)
                .withStatus(categoryStatus)
                .build();
        Mockito.when(categoriesService.getCategoriesForUser(userId)).thenReturn(List.of(movementCategory));

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model()
                        .attribute("categoriesList",Matchers
                                .hasItem(Matchers.allOf(
                                        Matchers.hasProperty("name",Matchers.is(movementCategoryName)),
                                        Matchers.hasProperty("active",Matchers.is(categoryStatus))
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

    @Test
    public void categoriesPage_correctGetRequest_enableLinkOnlyPresentForInactiveCategories()throws Exception{

        Long userId = someUserId();
        Long categoryId = 234L;
        String enableLink = HtmlUtils.htmlEscape(UriComponentsBuilder
                .fromUriString(getControllerBaseURL(userId).concat("/change_status"))
                .queryParam("id",categoryId)
                .queryParam("enable",true)
                .build()
                .toUriString());

        MovementCategory returnedCategory = TestCategoryBuilder.createValidCategory().withId(categoryId).withFlagActive(false).build();

        Mockito.when(categoriesService.getCategoriesForUser(userId)).thenReturn(List.of(returnedCategory));

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(enableLink)))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(">Enable<")))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString(">Disable<"))));
    }

    @Test
    public void categoriesPage_correctGetRequest_disableLinkOnlyPresentForActiveCategories()throws Exception{

        Long userId = someUserId();
        Long categoryId = 234L;
        String disableLink = HtmlUtils.htmlEscape(UriComponentsBuilder
                .fromUriString(getControllerBaseURL(userId).concat("/change_status"))
                .queryParam("id",categoryId)
                .queryParam("enable",false)
                .build()
                .toUriString());

        MovementCategory returnedCategory = TestCategoryBuilder.createValidCategory().withId(categoryId).withFlagActive(true).build();

        Mockito.when(categoriesService.getCategoriesForUser(userId)).thenReturn(List.of(returnedCategory));

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(disableLink)))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(">Disable<")))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString(">Enable<"))));
    }

    @Test
    public void categoriesChangeStatusLink_correctGetRequest_successfullyChangesStatusToEnabled() throws Exception{
        Long userId = someUserId();
        Long categoryId = 234L;

        String enableLink = UriComponentsBuilder
                .fromUriString(getControllerBaseURL(userId).concat("/change_status"))
                .queryParam("id",categoryId)
                .queryParam("enable",true)
                .build()
                .toUriString();

        mockMvc.perform(MockMvcRequestBuilders.get(enableLink))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());


        Mockito.verify(categoriesService).changeCategoryFlagActive(categoryId,true);
    }

    @Test
    public void categoriesChangeStatusLink_correctGetRequest_successfullyChangesStatusToDisabled() throws Exception{
        Long userId = someUserId();
        Long categoryId = 234L;

        String enableLink = UriComponentsBuilder
                .fromUriString(getControllerBaseURL(userId).concat("/change_status"))
                .queryParam("id",categoryId)
                .queryParam("enable",false)
                .build()
                .toUriString();

        mockMvc.perform(MockMvcRequestBuilders.get(enableLink))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());


        Mockito.verify(categoriesService).changeCategoryFlagActive(categoryId,false);
    }

    @Test
    public void categoriesChangeStatusLink_correctGetRequest_successfullyRedirectsToCategoriesPage() throws Exception{
        Long userId = someUserId();
        Long categoryId = 234L;

        String enableLink = UriComponentsBuilder
                .fromUriString(getControllerBaseURL(userId).concat("/change_status"))
                .queryParam("id",categoryId)
                .queryParam("enable",true)
                .build()
                .toUriString();

        mockMvc.perform(MockMvcRequestBuilders.get(enableLink))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl(getControllerBaseURL(userId)));

    }
    @Test
    public void categoriesChangeStatusLink_incorrectGetRequest_categoryIdMissing_throwsClientError() throws Exception{
        Long userId = someUserId();
        Long categoryId = 234L;

        String enableLink = UriComponentsBuilder
                .fromUriString(getControllerBaseURL(userId).concat("/change_status"))
                .queryParam("enable",true)
                .build()
                .toUriString();

        mockMvc.perform(MockMvcRequestBuilders.get(enableLink))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }
    @ParameterizedTest
    @ValueSource(booleans = {true,false})
    public void categoriesChangeStatusLink_incorrectGetRequest_categoryIdIncorrect_throwsClientError(Boolean enableQueryParam) throws Exception{
        Long userId = someUserId();
        Long categoryId = 234L;

        Mockito.doThrow(IncorrectCategoryIdException.class).when(categoriesService).changeCategoryFlagActive(categoryId,enableQueryParam);

        String enableLink = UriComponentsBuilder
                .fromUriString(getControllerBaseURL(userId).concat("/change_status"))
                .queryParam("id",categoryId)
                .queryParam("enable",enableQueryParam)
                .build()
                .toUriString();

        mockMvc.perform(MockMvcRequestBuilders.get(enableLink))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }
    @Test
    public void categoriesChangeStatusLink_incorrectGetRequest_enableFlagMissing_throwsClientError() throws Exception{
        Long userId = someUserId();
        Long categoryId = 234L;

        String enableLink = UriComponentsBuilder
                .fromUriString(getControllerBaseURL(userId).concat("/change_status"))
                .queryParam("id",categoryId)
                .build()
                .toUriString();

        mockMvc.perform(MockMvcRequestBuilders.get(enableLink))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }


    @ParameterizedTest
    @CsvSource({"-333,1,SALARY"})
    public void categoryFormEdit_correctGetRequest_inactiveCategory_formFieldsAreCorrectlyDisabled(Long userId,
                                                                             Long categoryId,
                                                                             String categoryName) throws Exception {

        MovementCategory categoryToEdit = TestCategoryBuilder.createValidCategory()
                .withId(categoryId)
                .withName(categoryName)
                .withFlagActive(false)
                .build();

        Mockito.when(categoriesService.getCategoryById(categoryId)).thenReturn(categoryToEdit);


        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(categoryId.toString())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute("disableFormFields",Matchers.is(true)))
                .andExpect(MockMvcResultMatchers.model().attribute("flagInactiveCategory",Matchers.is(true)))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Category disabled - edit not permitted")));
    }

    @ParameterizedTest
    @CsvSource({"777,4567,SALARY"})
    public void categoryFormSave_CorrectPostRequest_alreadyExistingCategory_originalEntityIsUsed(Long userId,
                                                                            Long categoryId,
                                                                            String name) throws Exception {

        MovementCategory originalMovement = TestCategoryBuilder.createValidCategory().withId(categoryId).build();
        Mockito.when(categoriesService.getCategoryById(categoryId))
                .thenReturn(originalMovement);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/save"))
                .with(csrf())
                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id",categoryId.toString())
                .param("name",name);

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

        ArgumentCaptor<MovementCategory> categoryArgumentCaptorArgumentCaptor = ArgumentCaptor.forClass(MovementCategory.class);
        Mockito.verify(categoriesService).saveCategory(categoryArgumentCaptorArgumentCaptor.capture());
        assertThat(categoryArgumentCaptorArgumentCaptor.getValue()).isSameAs(originalMovement);
        assertThat(categoryArgumentCaptorArgumentCaptor.getValue().getId()).isEqualTo(categoryId);
        assertThat(categoryArgumentCaptorArgumentCaptor.getValue().getName()).isEqualTo(name);
    }

    @Test
    public void categoriesPage_correctGetRequest_defaultCategory_editDisableDeleteButtonsAreNOTPresent() throws Exception{

        Long userId = someUserId();
        Integer expectedRecords = 8;

        String defaultCategoryName = "This is a default category";

        Mockito.when(categoriesService.getCategoriesForUser(userId))
                .thenReturn(Collections.nCopies(expectedRecords, TestCategoryBuilder.createValidCategory().withName(defaultCategoryName).build()));
        Mockito.when(categoriesService.isCategoryDefault(defaultCategoryName)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(defaultCategoryName)))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString(getControllerBaseURL(userId)+"/update"))))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString(getControllerBaseURL(userId)+"/change_status"))))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString(getControllerBaseURL(userId)+"/delete"))));
    }


    @Test
    public void categoriesDeleteConfirm_correctGetRequest_deleteServiceSuccessfullyCalled() throws Exception{

        Long userId = someUserId();
        Long categoryId = 1234L;
        Long categoryFallbackId = 5678L;

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/delete/confirm"))
                .with(csrf())
                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id",categoryId.toString())
                .param("name","ToDelete")
                .param("fallbackCategoryId",categoryFallbackId.toString());

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

        Mockito.verify(categoriesService).deleteCategoryById(categoryId);
    }

    @Test
    public void categoriesDeleteConfirm_incorrectGetRequest_incorrectCategoryId_throwsClientError() throws Exception{

        Long userId = someUserId();
        Long categoryId = 1234L;
        Long categoryFallbackId = 5678L;


        Mockito.doThrow(IncorrectCategoryIdException.class).when(categoriesService).deleteCategoryById(categoryId);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/delete/confirm"))
                .with(csrf())
                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id",categoryId.toString())
                .param("name","ToDelete")
                .param("fallbackCategoryId",categoryFallbackId.toString());

        mockMvc.perform(request)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

}
