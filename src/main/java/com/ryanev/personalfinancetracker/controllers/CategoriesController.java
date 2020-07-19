package com.ryanev.personalfinancetracker.controllers;

import com.ryanev.personalfinancetracker.dto.categories.CategoryFormDTO;
import com.ryanev.personalfinancetracker.dto.categories.CategoryViewDTO;
import com.ryanev.personalfinancetracker.dto.categories.implementations.CategoryVeiwDtoAdapter;
import com.ryanev.personalfinancetracker.dto.categories.implementations.DefaultCategoryFormDTO;
import com.ryanev.personalfinancetracker.entities.MovementCategory;
import com.ryanev.personalfinancetracker.entities.User;
import com.ryanev.personalfinancetracker.exceptions.IncorrectCategoryIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectMovementIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.exceptions.InvalidCategoryException;
import com.ryanev.personalfinancetracker.services.CategoriesService;
import com.ryanev.personalfinancetracker.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/{userId}/categories")
public class CategoriesController {

    @Autowired
    CategoriesService categoriesService;

    @Autowired
    UserService userService;

    private static final String controllerPath  = "categories";
    private static final String slash = "/";


    private String buildControllerBaseURL(long userId){
        return new StringBuilder(slash).append(userId).append(slash).append(controllerPath).toString();
    }

    private boolean checkIfUserExists(Long userId){
        try {
            User user = userService.getUserById(userId);
            return true;
        }
        catch (NoSuchElementException e){
            return false;
        }
    }

    @GetMapping
    public String categoriesLandingPage(Model model,
                                        @PathVariable("userId") Long userId) throws IncorrectUserIdException {

        if(!checkIfUserExists(userId)){
            throw new IncorrectUserIdException();
        }

        List<CategoryViewDTO> categoryViewDTOList = categoriesService.getCategoriesForUser(userId)
                .stream()
                .map(CategoryVeiwDtoAdapter::new)
                .collect(Collectors.toList());

        model.addAttribute("categoriesList",categoryViewDTOList);
        model.addAttribute("baseUrl",buildControllerBaseURL(userId));

        return "categories/categories-list";
    }


    @GetMapping("/new")
    public String newCategoryPage(Model model,
                                  @PathVariable("userId") Long userId) throws IncorrectUserIdException {

        if(!checkIfUserExists(userId)){
            throw new IncorrectUserIdException();
        }

        MovementCategory movementCategory = new MovementCategory();

        model = loadCategoryFormModel(model,userId,"New",movementCategory);

        return "categories/categories-form";
    }

    @GetMapping("/update")
    public String updateCategoryPage(Model model,
                                     @PathVariable("userId") Long userId,
                                     @RequestParam("id") Long categoryId) throws IncorrectUserIdException, IncorrectCategoryIdException {

        if(!checkIfUserExists(userId)){
            throw new IncorrectUserIdException();
        }

        MovementCategory movementCategory;
        try {
            movementCategory = categoriesService.getCategoryById(categoryId);
        }
        catch (NoSuchElementException e){
            throw new IncorrectCategoryIdException();
        }

        model = loadCategoryFormModel(model,userId,"Update",movementCategory);

        return "categories/categories-form";
    }

    @GetMapping("/delete")
    public String deleteCategoryPage(Model model,
                                     @PathVariable("userId") Long userId,
                                     @RequestParam("id") Long categoryId) throws IncorrectUserIdException {

        if(!checkIfUserExists(userId)){
            throw new IncorrectUserIdException();
        }

        MovementCategory movementCategory = categoriesService.getCategoryById(categoryId);

        model = loadCategoryFormModel(model,userId,"Delete",movementCategory);

        return "categories/categories-form";
    }

    private Model loadCategoryFormModel(Model model,
                                        Long userId,
                                        String action,
                                        MovementCategory movementCategory){

        String baseUrl = buildControllerBaseURL(userId);
        String okButtonUrl;
        CategoryFormDTO categoryFormEntry = new DefaultCategoryFormDTO(movementCategory);

        boolean disableFormFields;
        if (action.equals("Delete")){
            okButtonUrl = new StringBuilder(baseUrl).append(slash).append("delete").append(slash).append("confirm").toString();
            disableFormFields = true;
        }
        else {
            okButtonUrl = new StringBuilder(baseUrl).append(slash).append("save").toString();
            disableFormFields = false;
        }

        model.addAttribute("okButtonText",action);
        model.addAttribute("okButtonUrl",okButtonUrl);
        model.addAttribute("disableFormFields",disableFormFields);
        model.addAttribute("backButtonUrl",baseUrl);

        model.addAttribute("action",action);
        model.addAttribute("baseUrl",baseUrl);
        model.addAttribute("category",categoryFormEntry);
        model.addAttribute("uid",userId);

        return model;

    }

    @PostMapping("/save")
    public String saveCategoryPage(Model model,
                                   @PathVariable("userId") Long userId,
                                   CategoryFormDTO categoryFormDTO) throws InvalidCategoryException, IncorrectUserIdException {

        if(!checkIfUserExists(userId)){
            throw new IncorrectUserIdException();
        }

        MovementCategory movementCategory = new MovementCategory();

        movementCategory.setId(categoryFormDTO.getId());
        movementCategory.setName(categoryFormDTO.getName());
        movementCategory.setDescription(categoryFormDTO.getDescription());
        movementCategory.setUser(userService.getUserById(userId));
        movementCategory = categoriesService.saveCategory(movementCategory);

        return "redirect:"+buildControllerBaseURL(userId);
    }

}
