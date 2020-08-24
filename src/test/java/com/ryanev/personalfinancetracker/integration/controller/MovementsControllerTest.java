//package com.ryanev.personalfinancetracker.integration.controller;
//
//import com.ryanev.personalfinancetracker.data.entities.Movement;
//import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
//import com.ryanev.personalfinancetracker.data.entities.User;
//import com.ryanev.personalfinancetracker.services.categories.CategoriesService;
//import com.ryanev.personalfinancetracker.services.movements.MovementsService;
//import com.ryanev.personalfinancetracker.services.users.UserService;
//import org.hamcrest.Matchers;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.CsvSource;
//import org.junit.jupiter.params.provider.ValueSource;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.jdbc.Sql;
//import org.springframework.test.context.jdbc.SqlGroup;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//
//import java.time.LocalDate;
//import java.util.List;
//
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@SqlGroup({@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,scripts = {"classpath:clear_db.sql","classpath:db_create.sql","classpath:test_data_insert.sql"}),
//            @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,scripts = {"classpath:clear_db.sql"})})
//public class MovementsControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//    @Autowired
//    private UserService userService;
//    @Autowired
//    private CategoriesService categoriesService;
//    @Autowired
//    private MovementsService movementsService;
//
//
//    private List<User> users;
//    private List<MovementCategory> movementCategories;
//    private List<Movement> movements;
//
//    @BeforeEach
//    private void fillEntities(){
//        users = userService.getAll();
//        movementCategories = categoriesService.getAll();
//        movements = movementsService.getAll();
//    }
//
//    private String getControllerBaseURL(Long userId){
//        return "/"+userId+"/movements";
//    }
//    private Long getUserId(String username){
//        return users.stream()
//                .filter(f->f.getUsername().equals(username))
//                .findFirst().orElseThrow()
//                .getId();
//    }
//    private Long getCategoryId(Long userId, String movementCategoryName) {
//        return movementCategories.stream()
//                .filter(f -> f.getUser().getId().equals(userId))
//                .filter(f -> f.getName().equals(movementCategoryName))
//                .findFirst().orElseThrow()
//                .getId();
//    }
//
//    @ParameterizedTest
//    @CsvSource({"testuser1,6","testuser2,6"})
//    public void movementsPageCorrectGetRequestValidateAllMovementsForUserArePresent(String userName, Integer expectedValue) throws Exception{
//        Long userId = getUserId(userName);
//
//        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.model().attribute("movementsList", Matchers.iterableWithSize(expectedValue)));
//    }
//
//    @ParameterizedTest
//    @CsvSource({"testuser1,Salary june,5000,2020,6,15,SALARY",
//            "testuser1,Lunch: Happy,-57.23,2020,5,17,OTHER",
//            "testuser1,Steam Bundle Purchase,-120,2020,6,18,Games"})
//    public void movementsPageCorrectGetRequestValidateIfDataOnASingleMovementIsCorrect(String userName,
//                                                                                       String movementName,
//                                                                                       Double movementAmount,
//                                                                                       Integer movementYear,
//                                                                                       Integer movementMonth,
//                                                                                       Integer movementDay,
//                                                                                       String movementCategoryName) throws Exception{
//        Long userId = getUserId(userName);
//
//        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.model()
//                        .attribute("movementsList",Matchers
//                                .hasItem(Matchers.allOf(
//                                        Matchers.hasProperty("name",Matchers.is(movementName)),
//                                        Matchers.hasProperty("signedAmount",Matchers.is(movementAmount)),
//                                        Matchers.hasProperty("valueDate",Matchers.is(LocalDate.of(movementYear,movementMonth,movementDay))),
//                                        Matchers.hasProperty("categoryName",Matchers.is(movementCategoryName))
//                                ))
//                        ));
//    }
//
//    @ParameterizedTest
//    @ValueSource(strings = {"testuser1","testuser2"})
//    public void movementsPageCorrectGetRequestValidateIfNewMovementButtonIsPresent(String userName) throws Exception{
//        Long userId = getUserId(userName);
//
//        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content()
//                        .string(Matchers.containsString(getControllerBaseURL(userId).concat("/new"))));
//    }
//
//    @ParameterizedTest
//    @ValueSource(strings = {"testuser1","testuser2"})
//    //TODO: we expect each movement to have Edit and Delete links. see how
//    public void movementsPageCorrectGetRequestValidateIfEditAndDeleteButtonsArePresent(String userName) throws Exception{
//        Long userId = getUserId(userName);
//
//        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(getControllerBaseURL(userId)+"/update")))
//                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(getControllerBaseURL(userId)+"/delete")));
//    }
//
//    //TODO: add the ability to sort the movements page records and return them in reverse order
//
//    @ParameterizedTest
//    @ValueSource(longs = {-22,3456,77,22222,1234567456})
//    public void movementsPageIncorrectGetRequestIncorrectUserIdValidateErrorIsThrown(Long userId) throws Exception{
//        Long numberOfMatchingUsers = users.stream().filter(f -> f.getId().equals(userId)).count();
//        if(numberOfMatchingUsers!=0){
//            return;
//        }
//
//        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)))
//                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
//    }
//
//    @ParameterizedTest
//    @ValueSource(strings = {"id","valueDate","flagAmountPositive","unsignedAmount","name","description","categoryId"})
//    //TODO there is probably a better way to do this - check all fields without performing to many get requests
//    public void movementFormNewCorrectGetRequestValidateAllFormFieldsArePresent(String fieldId) throws Exception{
//        Long userId = getUserId("testuser1");
//        String testString = "id=\""+fieldId+"\"";
//
//        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/new")))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(testString)));
//    }
//
//    @Test
//    public void movementFormNewCorrectGetRequestValidateSaveLinkIsPresent() throws Exception{
//        Long userId = getUserId("testuser1");
//
//        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/new")))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(getControllerBaseURL(userId).concat("/save"))));
//    }
//
//    @Test
//    public void movementFormNewCorrectGetRequestValidateBackLinkIsPresent() throws Exception{
//        Long userId = getUserId("testuser1");
//
//        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/new")))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(getControllerBaseURL(userId))));
//    }
//
//    @ParameterizedTest
//    @ValueSource(longs = {-22,3456,77,22222,1234567456})
//    public void movementFormNewIncorrectGetRequestIncorrectUserIdValidateCorrectExceptionIsThrown(Long userId) throws Exception{
//        Long numberOfMatchingUsers = users.stream().filter(f -> f.getId().equals(userId)).count();
//        if(numberOfMatchingUsers!=0){
//            return;
//        }
//
//        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/new")))
//                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
//    }
//
//    @ParameterizedTest
//    @CsvSource({"testuser1,1,2020,6,15,SALARY,Salary june,5000,true,Salary June"})
//    public void movementFormEditCorrectGetRequestValidateFormDataIsCorrectlyLoaded(String userName,
//                                                                                   Long movementId,
//                                                                                   Integer movementYear,
//                                                                                   Integer movementMonth,
//                                                                                   Integer movementDay,
//                                                                                   String movementCategoryName,
//                                                                                   String movementName,
//                                                                                   Double movementUnsignedAmount,
//                                                                                   Boolean movementSignPositive,
//                                                                                   String movementDescription) throws Exception {
//        Long userId = getUserId(userName);
//        LocalDate movementDate = LocalDate.of(movementYear,movementMonth,movementDay);
//        Long categoryId = getCategoryId(userId,movementCategoryName);
//        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(movementId.toString())))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers
//                        .model()
//                        .attribute("movement", Matchers.allOf(
//                                Matchers.hasProperty("id",Matchers.is(movementId)),
//            Matchers.hasProperty("unsignedAmount",Matchers.is(movementUnsignedAmount)),
//            Matchers.hasProperty("valueDate",Matchers.is(movementDate)),
//            Matchers.hasProperty("name",Matchers.is(movementName)),
//            Matchers.hasProperty("flagAmountPositive",Matchers.is(movementSignPositive)),
//            Matchers.hasProperty("categoryId",Matchers.is(categoryId)),
//            Matchers.hasProperty("description",Matchers.is(movementDescription))))
//            );
//    }
//
//    @ParameterizedTest
//    @CsvSource({"testuser1,1","testuser1,2","testuser1,3"})
//    public void movementFormEditCorrectGetRequestValidateSaveLinkIsPresent(String userName, Long movementId) throws Exception{
//        Long userId = getUserId(userName);
//
//        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(movementId.toString())))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(getControllerBaseURL(userId).concat("/save"))));
//    }
//
//    @ParameterizedTest
//    @CsvSource({"42,1","-777,2","666,3"})
//    public void movementFormEditIncorrectGetRequestInvalidUserId(Long userId, Long movementId) throws Exception{
//        Long numberOfMatchingUsers = users.stream().filter(f -> f.getId().equals(userId)).count();
//        if(numberOfMatchingUsers!=0){
//            return;
//        }
//
//        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(movementId.toString())))
//                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
//    }
//
//    @ParameterizedTest
//    @CsvSource({"testuser1","testuser2"})
//    public void movementFormEditIncorrectGetRequestMissingMovementId(String userName) throws Exception{
//        Long userId = getUserId(userName);
//
//        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update")))
//                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
//    }
//
//    @ParameterizedTest
//    @CsvSource({"testuser1,66","testuser2,99"})
//    public void movementFormEditIncorrectGetRequestIncorrectMovementId(String userName, Long movementId) throws Exception{
//        Long userId = getUserId(userName);
//
//        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/update").concat("?id=").concat(movementId.toString())))
//                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
//    }
//
//    @ParameterizedTest
//    @CsvSource({"testuser1,1,2020,6,15,SALARY,Salary june,5000,true,Salary June"})
//    //todo add more scenarios
//    public void movementFormDeleteCorrectGetRequestValidateAllDataIsCorrectlyLoaded(String userName,
//                                                                                    Long movementId,
//                                                                                    Integer movementYear,
//                                                                                    Integer movementMonth,
//                                                                                    Integer movementDay,
//                                                                                    String movementCategoryName,
//                                                                                    String movementName,
//                                                                                    Double movementUnsignedAmount,
//                                                                                    Boolean movementSignPositive,
//                                                                                    String movementDescription) throws Exception {
//        Long userId = getUserId(userName);
//        LocalDate movementDate = LocalDate.of(movementYear,movementMonth,movementDay);
//        Long categoryId = getCategoryId(userId,movementCategoryName);
//        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/delete").concat("?id=").concat(movementId.toString())))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers
//                        .model()
//                        .attribute("movement", Matchers.allOf(
//                                Matchers.hasProperty("id",Matchers.is(movementId)),
//                                Matchers.hasProperty("unsignedAmount",Matchers.is(movementUnsignedAmount)),
//                                Matchers.hasProperty("valueDate",Matchers.is(movementDate)),
//                                Matchers.hasProperty("name",Matchers.is(movementName)),
//                                Matchers.hasProperty("flagAmountPositive",Matchers.is(movementSignPositive)),
//                                Matchers.hasProperty("categoryId",Matchers.is(categoryId)),
//                                Matchers.hasProperty("description",Matchers.is(movementDescription))))
//                );
//    }
//
//    //    movement form delete - correct get request - validate all data is correctly disabled
//    @ParameterizedTest
//    @CsvSource({"testuser1,1","testuser1,2","testuser1,3","testuser2,4","testuser2,5"})
//    public void movementFormDeleteCorrectGetRequestValidateAllDataIsCorrectlyDisabled(String userName,Long movementId) throws Exception {
//        Long userId = getUserId(userName);
//
//        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/delete").concat("?id=").concat(movementId.toString())))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers
//                        .model()
//                        .attribute("disableFormFields",Matchers.is(true)));
//    }
//
////    movement form delete - correct get request - validate confirm delete link is present
//    @ParameterizedTest
//    @CsvSource({"testuser1,1","testuser1,2","testuser1,3","testuser2,4","testuser2,5"})
//    public void movementFormDeleteCorrectGetRequestValidateConfirmDeleteLinkIsPresent(String userName,Long movementId) throws Exception{
//        Long userId = getUserId(userName);
//
//        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/delete").concat("?id=").concat(movementId.toString())))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers
//                        .content().string(Matchers.containsString(getControllerBaseURL(userId).concat("/delete/confirm"))));
//    }
//
////    movement form delete - incorrect get request - incorrect user id
//    @ParameterizedTest
//    @CsvSource({"42,1","-777,2","666,3"})
//    public void movementFormDeleteIncorrectGetRequestIncorrectUserId(Long userId, Long movementId) throws Exception {
//        Long numberOfMatchingUsers = users.stream().filter(f -> f.getId().equals(userId)).count();
//        if(numberOfMatchingUsers!=0){
//            return;
//        }
//
//        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/delete").concat("?id=").concat(movementId.toString())))
//                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
//    }
//
//    //    movement form delete - incorrect get request - missing movement id
//    @ParameterizedTest
//    @CsvSource({"testuser1","testuser2"})
//    public void movementFormDeleteIncorrectGetRequestMissingMovementId(String userName) throws Exception{
//        Long userId = getUserId(userName);
//
//        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/delete")))
//                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
//    }
//
////    movement form delete - incorrect get request - incorrect movement id
//    @ParameterizedTest
//    @CsvSource({"testuser1,66","testuser2,99"})
//    public void movementFormDeleteIncorrectGetRequestIncorrectMovementId(String userName, Long movementId) throws Exception{
//        Long userId = getUserId(userName);
//
//        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId).concat("/delete").concat("?id=").concat(movementId.toString())))
//                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
//    }
//
////    movement form save - correct post request - validate if data is correctly inserted
//    @ParameterizedTest
//    @CsvSource({"testuser1,2020-01-15,First Salary of the Year,true,9000,New year new salary,SALARY"})
//    public void movementFormSave_CorrectPostRequest_RequestIsCorrectlyAccepted(String userName,
//                                                                                    String valueDate,
//                                                                                    String name,
//                                                                                    String flagAmountPositive,
//                                                                                    String unsignedAmount,
//                                                                                    String description,
//                                                                                    String categoryName) throws Exception {
//        Long userId = getUserId(userName);
//        Long categoryId = getCategoryId(userId,categoryName);
//        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(getControllerBaseURL(userId).concat("/save"))
//                .with(csrf())
//                .accept(MediaType.TEXT_HTML,MediaType.APPLICATION_XHTML_XML)
//                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//                .param("id","")
//                .param("valueDate",valueDate)
//                .param("name",name)
//                .param("flagAmountPositive",flagAmountPositive)
//                .param("unsignedAmount",unsignedAmount)
//                .param("description",description)
//                .param("categoryId",categoryId.toString());
//
//        mockMvc.perform(request)
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
//                .andExpect(MockMvcResultMatchers.view().name("redirect:".concat(getControllerBaseURL(userId))));
//    }
//
////    movement form save - correct post request - validate if data is correctly edited
////    movement form save - incorrect post request - amount is empty
////    movement form save - incorrect post request - value date is empty
////    movement form save - incorrect post request - value date is incorrect format
////    movement form save - incorrect post request - name is empty
////    movement form save - incorrect post request - userId is empty
////    movement form save - incorrect post request - userId is incorrect
////    movement form save - incorrect post request - categoryId is empty
////    movement form save - incorrect post request - categoryId is incorrect
//}
