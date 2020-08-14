package com.ryanev.personalfinancetracker.web.controllers;

import com.ryanev.personalfinancetracker.web.dto.categories.CategoryFormDTO;
import com.ryanev.personalfinancetracker.web.dto.categories.CategoryViewDTO;
import com.ryanev.personalfinancetracker.web.dto.categories.implementations.CategoryVeiwDtoConcrete;
import com.ryanev.personalfinancetracker.web.dto.categories.implementations.DefaultCategoryFormDTO;
import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import com.ryanev.personalfinancetracker.data.entities.User;
import com.ryanev.personalfinancetracker.exceptions.IncorrectCategoryIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.exceptions.InvalidCategoryException;
import com.ryanev.personalfinancetracker.services.categories.CategoriesService;
import com.ryanev.personalfinancetracker.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@SuppressWarnings({"ALL", "SameReturnValue"})
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
                .map(category -> new CategoryVeiwDtoConcrete(category,
                        categoriesService.isCategoryDefault(category.getName()),
                        buildControllerBaseURL(userId)))
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
        List<MovementCategory> defaultCategories = categoriesService.getDefaultCategoriesForUser(userId);

        model = loadCategoryFormModel(model,userId,"New",movementCategory,defaultCategories);

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
        List<MovementCategory> defaultCategories = categoriesService.getDefaultCategoriesForUser(userId);

        model = loadCategoryFormModel(model,userId,"Update",movementCategory,defaultCategories);

        return "categories/categories-form";
    }

    @SuppressWarnings("SameReturnValue")
    @GetMapping("/delete")
    public String deleteCategoryPage(Model model,
                                     @PathVariable("userId") Long userId,
                                     @RequestParam("id") Long categoryId) throws IncorrectUserIdException {

        if(!checkIfUserExists(userId)){
            throw new IncorrectUserIdException();
        }

        MovementCategory movementCategory = categoriesService.getCategoryById(categoryId);
        List<MovementCategory> defaultCategories = categoriesService.getDefaultCategoriesForUser(userId);
        model = loadCategoryFormModel(model,userId,"Delete",movementCategory,defaultCategories);

        return "categories/categories-form";
    }

    private Model loadCategoryFormModel(Model model,
                                        Long userId,
                                        String action,
                                        MovementCategory movementCategory,
                                        List<MovementCategory> defaultCategories){

        String baseUrl = buildControllerBaseURL(userId);
        String okButtonUrl;
        CategoryFormDTO categoryFormEntry = new DefaultCategoryFormDTO(movementCategory);

        boolean disableFormFields;
        boolean flagInactiveCategory = false;
        if (action.equals("Delete")){
            okButtonUrl = new StringBuilder(baseUrl).append(slash).append("delete").append(slash).append("confirm").toString();
            disableFormFields = true;
        }
        else {
            okButtonUrl = new StringBuilder(baseUrl).append(slash).append("save").toString();

            if(movementCategory.getFlagActive() != null && !movementCategory.getFlagActive()){
                disableFormFields = true;
                flagInactiveCategory = true;
            }else {
                disableFormFields = false;
            }

        }


        model.addAttribute("fallbackCategories",defaultCategories);
        model.addAttribute("flagInactiveCategory",flagInactiveCategory);
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


        MovementCategory movementCategory;

        if (categoryFormDTO.getId() == null){
            movementCategory = new MovementCategory();
            movementCategory.setFlagActive(true);
        }
        else {
            movementCategory = categoriesService.getCategoryById(categoryFormDTO.getId());
        }

        movementCategory.setName(categoryFormDTO.getName());
        movementCategory.setDescription(categoryFormDTO.getDescription());
        movementCategory.setUser(userService.getUserById(userId));
        movementCategory.setFallbackCategoryId(categoryFormDTO.getFallbackCategoryId());
        movementCategory = categoriesService.saveCategory(movementCategory);

        return "redirect:"+buildControllerBaseURL(userId);
    }

    @PostMapping("/delete/confirm")
    public String confirmDeleteOfCategory(Model model,
                                          @PathVariable("userId") Long userId,
                                          CategoryFormDTO categoryFormDTO) throws IncorrectUserIdException, IncorrectCategoryIdException {
        if(!checkIfUserExists(userId)){
            throw new IncorrectUserIdException();
        }

        categoriesService.deleteCategoryById(categoryFormDTO.getId());

        return "redirect:"+buildControllerBaseURL(userId);
    }

    @GetMapping("change_status")
    public String updateCategoryStatus(Model model,
                                       @PathVariable("userId") Long userId,
                                       @RequestParam("id") Long categoryId,
                                       @RequestParam("enable") Boolean flagValue) throws IncorrectCategoryIdException {

        categoriesService.changeCategoryFlagActive(categoryId,flagValue);

        return "redirect:"+buildControllerBaseURL(userId);
    }

}
