package com.ryanev.personalfinancetracker.web.controllers;

import com.ryanev.personalfinancetracker.services.dto.categories.CategoryDTO;
import com.ryanev.personalfinancetracker.web.dto.categories.CategoryFormDTO;
import com.ryanev.personalfinancetracker.web.dto.categories.CategoryViewDTO;
import com.ryanev.personalfinancetracker.exceptions.IncorrectCategoryIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.exceptions.InvalidCategoryException;
import com.ryanev.personalfinancetracker.services.categories.CategoriesService;
import com.ryanev.personalfinancetracker.services.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;
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

    private final String ENABLE = "Enable";
    private final String DISABLE = "Disable";
    private final String ACTIVE = "Active";
    private final String DISABLED = "Disabled";


    private String buildControllerBaseURL(long userId){
        return new StringBuilder(slash).append(userId).append(slash).append(controllerPath).toString();
    }

    private CategoryViewDTO mapCategoryToViewDTO(CategoryDTO categoryDTO){

        String baseUri = buildControllerBaseURL(categoryDTO.getUserId());

        String enableDisableLink = UriComponentsBuilder.fromUriString(baseUri.concat("/change_status"))
                .queryParam("id",categoryDTO.getId())
                .queryParam("enable",!categoryDTO.getFlagActive())
                .build()
                .toUriString();

        String updateLink = UriComponentsBuilder.fromUriString(baseUri.concat("/update"))
                .queryParam("id",categoryDTO.getId()).build().toUriString();

        String deleteLink = UriComponentsBuilder.fromUriString(baseUri.concat("/delete"))
                .queryParam("id",categoryDTO.getId()).build().toUriString();

        CategoryViewDTO newDTO = new CategoryViewDTO();
        newDTO.setName(categoryDTO.getName());
        newDTO.setActive(categoryDTO.getFlagActive()?ACTIVE:DISABLED);
        newDTO.setDeleteLink(deleteLink);
        newDTO.setUpdateLink(updateLink);
        newDTO.setEnableDisableLink(enableDisableLink);
        newDTO.setEnableDisableText(categoryDTO.getFlagActive()?DISABLE:ENABLE);
        newDTO.setFlagDefault(categoriesService.isCategoryDefault(categoryDTO.getName()));

        return newDTO;
    }
    private CategoryFormDTO mapCategoryToFormDTO(CategoryDTO categoryDTO){
        CategoryFormDTO newDTO = new CategoryFormDTO();
        newDTO.setName(categoryDTO.getName());
        newDTO.setId(categoryDTO.getId());
        newDTO.setDescription(categoryDTO.getDescription());
        newDTO.setFallbackCategoryId(categoryDTO.getFallbackCategoryId());

        return newDTO;
    }

    @GetMapping
    public String categoriesLandingPage(Model model,
                                        @PathVariable("userId") Long userId) throws IncorrectUserIdException {

        if(!userService.existsById(userId)){
            throw new IncorrectUserIdException();
        }

        List<CategoryViewDTO> categoryViewDTOList = categoriesService.getCategoriesForUser(userId)
                .stream()
                .map(this::mapCategoryToViewDTO)
                .sorted(Comparator
                        .comparing(CategoryViewDTO::getFlagDefault)
                        .thenComparing(CategoryViewDTO::getActive)
                        .thenComparing(CategoryViewDTO::getName))
                .collect(Collectors.toList());

        model.addAttribute("categoriesList",categoryViewDTOList);
        model.addAttribute("baseUrl",buildControllerBaseURL(userId));

        return "categories/categories-list";
    }


    @GetMapping("/new")
    public String newCategoryPage(Model model,
                                  @PathVariable("userId") Long userId) throws IncorrectUserIdException {

        if(!userService.existsById(userId)){
            throw new IncorrectUserIdException();
        }

        CategoryDTO movementCategory = new CategoryDTO();
        List<CategoryDTO> defaultCategories = categoriesService.getDefaultCategoriesForUser(userId);

        loadCategoryFormModel(model,userId,"New",movementCategory,defaultCategories);

        return "categories/categories-form";
    }

    @GetMapping("/update")
    public String updateCategoryPage(Model model,
                                     @PathVariable("userId") Long userId,
                                     @RequestParam("id") Long categoryId) throws IncorrectUserIdException, IncorrectCategoryIdException {

        if(!userService.existsById(userId)){
            throw new IncorrectUserIdException();
        }

        CategoryDTO movementCategory;
        try {
            movementCategory = categoriesService.getCategoryById(categoryId);
        }
        catch (NoSuchElementException e){
            throw new IncorrectCategoryIdException();
        }
        List<CategoryDTO> defaultCategories = categoriesService.getDefaultCategoriesForUser(userId);

        loadCategoryFormModel(model,userId,"Update",movementCategory,defaultCategories);

        return "categories/categories-form";
    }

    @GetMapping("/delete")
    public String deleteCategoryPage(Model model,
                                     @PathVariable("userId") Long userId,
                                     @RequestParam("id") Long categoryId) throws IncorrectUserIdException {

        if(!userService.existsById(userId)){
            throw new IncorrectUserIdException();
        }

        CategoryDTO movementCategory = categoriesService.getCategoryById(categoryId);
        List<CategoryDTO> defaultCategories = categoriesService.getDefaultCategoriesForUser(userId);

        loadCategoryFormModel(model,userId,"Delete",movementCategory,defaultCategories);

        return "categories/categories-form";
    }

    private void loadCategoryFormModel(Model model,
                                        Long userId,
                                        String action,
                                        CategoryDTO movementCategory,
                                        List<CategoryDTO> defaultCategories){

        String baseUrl = buildControllerBaseURL(userId);
        String okButtonUrl;

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

        if(!model.containsAttribute("category")){
            CategoryFormDTO categoryFormDTO = mapCategoryToFormDTO(movementCategory);
            model.addAttribute("category",categoryFormDTO);
        }

        model.addAttribute("fallbackCategories",defaultCategories);
        model.addAttribute("flagInactiveCategory",flagInactiveCategory);
        model.addAttribute("okButtonText",action);
        model.addAttribute("okButtonUrl",okButtonUrl);
        model.addAttribute("disableFormFields",disableFormFields);
        model.addAttribute("backButtonUrl",baseUrl);
        model.addAttribute("action",action);
        model.addAttribute("baseUrl",baseUrl);
        model.addAttribute("uid",userId);
    }

    @PostMapping("/save")
    public String saveCategoryPage(Model model,
                                   @PathVariable("userId") Long userId,
                                   @ModelAttribute("category") @Valid CategoryFormDTO categoryFormDTO,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) throws InvalidCategoryException, IncorrectUserIdException {

        if(!userService.existsById(userId)){
            throw new IncorrectUserIdException();
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.category", bindingResult);
            redirectAttributes.addFlashAttribute("category", categoryFormDTO);
            if (categoryFormDTO.getId()!=null){
                return "redirect:"+buildControllerBaseURL(userId)+"/update?id="+categoryFormDTO.getId();
            }
            else {
                return "redirect:"+buildControllerBaseURL(userId)+"/new";
            }
        }

        CategoryDTO serviceDTO = mapFormDtoToCategory(userId, categoryFormDTO);

        categoriesService.saveCategory(serviceDTO);

        return "redirect:"+buildControllerBaseURL(userId);
    }

    private CategoryDTO mapFormDtoToCategory(@PathVariable("userId") Long userId, CategoryFormDTO categoryFormDTO) {
        CategoryDTO serviceDTO;
        if(categoryFormDTO.getId()!=null){
            serviceDTO = categoriesService.getCategoryById(categoryFormDTO.getId());
        }
        else {
            serviceDTO = new CategoryDTO();
            serviceDTO.setUserId(userId);
            serviceDTO.setFlagActive(true);
        }

        serviceDTO.setName(categoryFormDTO.getName());
        serviceDTO.setDescription(categoryFormDTO.getDescription());
        serviceDTO.setFallbackCategoryId(categoryFormDTO.getFallbackCategoryId());
        return serviceDTO;
    }

    @PostMapping("/delete/confirm")
    public String confirmDeleteOfCategory(Model model,
                                          @PathVariable("userId") Long userId,
                                          @ModelAttribute("category") CategoryFormDTO categoryFormDTO) throws IncorrectUserIdException, IncorrectCategoryIdException {
        if(!userService.existsById(userId)){
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
