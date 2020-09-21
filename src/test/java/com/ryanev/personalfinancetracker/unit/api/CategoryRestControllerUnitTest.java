package com.ryanev.personalfinancetracker.unit.api;


import com.ryanev.personalfinancetracker.data.entities.User;
import com.ryanev.personalfinancetracker.services.categories.CategoriesService;
import com.ryanev.personalfinancetracker.services.dto.categories.CategoryDTO;
import com.ryanev.personalfinancetracker.services.users.UserService;
import com.ryanev.personalfinancetracker.util.TestCategoryBuilder;
import com.ryanev.personalfinancetracker.util.user.TestUserBuilder;
import com.ryanev.personalfinancetracker.web.api.CategoriesRestController;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.NoSuchElementException;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CategoriesRestController.class)
public class CategoryRestControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @MockBean
    private CategoriesService categoriesService;


    private String getControllerBaseURL(Long userId){
        return "/api/"+userId+"/categories";
    }
    private Long someUserId(){
        return 888777666L;
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
    public void getCategoryByName_correctGetRequest_valuePresent_validJsonIsReturned() throws Exception {

        Long userId = someUserId();
        String categoryName = "SomeCategory";
        String userName = "testUser";

        mockUser("testUser",userId,true);

        CategoryDTO categoryDTO = TestCategoryBuilder
                .createValidCategory()
                .withName(categoryName)
                .withFlagActive(true)
                .withUser(TestUserBuilder.createValidUser().withUsername(userName).withId(userId).build())
                .buildDTO();

        Mockito.when(categoriesService.getCategoryByNameAndUserId(categoryName,userId)).thenReturn(categoryDTO);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)+"/"+categoryName))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

    }

    @Test
    @WithMockUser("testUser")
    public void getCategoryByName_correctGetRequest_valueNotFound_404Returned() throws Exception {

        Long userId = someUserId();
        String categoryName = "SomeCategory";
        String userName = "testUser";

        mockUser("testUser",userId,true);

        Mockito.when(categoriesService.getCategoryByNameAndUserId(categoryName,userId)).thenThrow(NoSuchElementException.class);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)+"/"+categoryName))
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    @ParameterizedTest
    @ValueSource(longs = {-22,3456,77,22222,1234567456})
    @WithMockUser("testUser")
    public void getCategoryByName_incorrectGetRequest_attemptingToAccessDifferentUserData_forbiddenErrorIsThrown(Long userId) throws Exception {

        String categoryName = "SomeCategory";
        String userName = "testUser";
        Long loggedUser = 23941L;

        mockUser("testUser",loggedUser,true);
        if(userId.equals(loggedUser)){
            throw new RuntimeException("Passed user can't be the same as logged user");
        }

        CategoryDTO categoryDTO = TestCategoryBuilder
                .createValidCategory()
                .withName(categoryName)
                .withFlagActive(true)
                .withUser(TestUserBuilder.createValidUser().withId(userId).build())
                .buildDTO();

        Mockito.when(categoriesService.getCategoryByNameAndUserId(categoryName,userId)).thenReturn(categoryDTO);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)+"/"+categoryName))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void getCategoryByName_incorrectGetRequest_unauthorized_unauthorizedErrorIsThrown() throws Exception {

        Long userId = someUserId();
        String categoryName = "SomeCategory";

        CategoryDTO categoryDTO = TestCategoryBuilder
                .createValidCategory()
                .withName(categoryName)
                .withFlagActive(true)
                .withUser(TestUserBuilder.createValidUser().withId(userId).build())
                .buildDTO();

        Mockito.when(categoriesService.getCategoryByNameAndUserId(categoryName,userId)).thenReturn(categoryDTO);

        mockMvc.perform(MockMvcRequestBuilders.get(getControllerBaseURL(userId)+"/"+categoryName))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

}
