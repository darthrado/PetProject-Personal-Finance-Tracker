package com.ryanev.personalfinancetracker.web.api;

import com.ryanev.personalfinancetracker.exceptions.IncorrectCategoryIdException;
import com.ryanev.personalfinancetracker.exceptions.InvalidCategoryException;
import com.ryanev.personalfinancetracker.services.categories.CategoriesService;
import com.ryanev.personalfinancetracker.services.dto.categories.CategoryDTO;
import com.ryanev.personalfinancetracker.web.dto.categories.CategoryApiDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/{userId}/categories")
public class CategoriesRestController {

    @Autowired
    CategoriesService categoriesService;

    @GetMapping("/{categoryName}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody CategoryApiDTO getCategoryByName(@PathVariable("userId") Long userId,
                                                          @PathVariable("categoryName") String categoryName){
        CategoryApiDTO apiDTO;

        try {
            CategoryDTO categoryDTO = categoriesService.getCategoryByNameAndUserId(categoryName, userId);
            apiDTO = buildApiDTO(categoryDTO);

        }
        catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }


        return apiDTO;
    }

    @PostMapping("/{categoryName}/saveNew")
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody CategoryApiDTO saveNewCategory(@PathVariable("userId") Long userId,
                                     @PathVariable("categoryName") String categoryName) throws InvalidCategoryException {

        if(categoriesService.existsByNameAndUserId(categoryName,userId)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Category already exists");
        }

        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName(categoryName);
        categoryDTO.setDescription("Automatically Generated from movement screeen...");
        categoryDTO.setFallbackCategoryId(categoriesService.getDefaultCategoriesForUser(userId).get(0).getId());
        categoryDTO.setFlagActive(true);
        categoryDTO.setUserId(userId);

        categoriesService.saveCategory(categoryDTO);

        CategoryApiDTO apiDTO = buildApiDTO(categoryDTO);

        return apiDTO;

    }

    @PutMapping("/{categoryName}/enable")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody CategoryApiDTO enableCategory(@PathVariable("userId") Long userId,
                                 @PathVariable("categoryName") String categoryName) throws IncorrectCategoryIdException {

        CategoryApiDTO apiDTO;
        try {
            CategoryDTO categoryDTO = categoriesService.getCategoryByNameAndUserId(categoryName,userId);
            categoriesService.changeCategoryFlagActive(categoryDTO.getId(),true);
            apiDTO = buildApiDTO(categoryDTO);
        }
        catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

      return apiDTO;
    }

    private CategoryApiDTO buildApiDTO(CategoryDTO categoryDTO){
        CategoryApiDTO apiDTO = new CategoryApiDTO();
        apiDTO.setName(categoryDTO.getName());
        apiDTO.setFlagActive(categoryDTO.getFlagActive());

        return apiDTO;
    }

}
