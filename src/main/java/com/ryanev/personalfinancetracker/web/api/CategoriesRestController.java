package com.ryanev.personalfinancetracker.web.api;

import com.ryanev.personalfinancetracker.services.categories.CategoriesService;
import com.ryanev.personalfinancetracker.services.dto.categories.CategoryDTO;
import com.ryanev.personalfinancetracker.web.dto.categories.CategoryApiDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/{userId}/api/categories")
public class CategoriesRestController {

    @Autowired
    CategoriesService categoriesService;

    @GetMapping("/{categoryName}")
    public @ResponseBody CategoryApiDTO getCategoryByName(@PathVariable("userId") Long userId,
                                                          @PathVariable("categoryName") String categoryName){
        CategoryApiDTO apiDTO;

        try {
            CategoryDTO categoryDTO = categoriesService.getCategoryByNameAndUserId(categoryName, userId);
            apiDTO = new CategoryApiDTO();
            apiDTO.setName(categoryDTO.getName());
            apiDTO.setFlagActive(categoryDTO.getFlagActive());

        }
        catch (NoSuchElementException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }


        return apiDTO;
    }

    @PostMapping("/{categoryName}/saveNew")
    public String getSaveNewCategory(@PathVariable("userId") Long userId,
                                     @PathVariable("categoryName") String categoryName){
        return "";
    }

    @PutMapping("/{categoryName}/enable")
    public String enableCategory(@PathVariable("userId") Long userId,
                                 @PathVariable("categoryName") String categoryName){
        return "";
    }


}
