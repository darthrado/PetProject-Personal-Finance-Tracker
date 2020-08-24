package com.ryanev.personalfinancetracker.services.categories;

import com.ryanev.personalfinancetracker.data.repo.categories.CategoriesRepository;
import com.ryanev.personalfinancetracker.data.repo.users.UserRepository;
import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import com.ryanev.personalfinancetracker.exceptions.IncorrectCategoryIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.exceptions.InvalidCategoryException;
import com.ryanev.personalfinancetracker.services.crud_observer.CrudChangeNotifier;
import com.ryanev.personalfinancetracker.services.dto.categories.CategoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class DefaultCategoriesService implements CategoriesService {
    @Autowired
    private CategoriesRepository categoriesRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryChangeNotifier categoryChangeNotifier;

    private final List<String> defaultCategoryNames = List.of("Income(DEFAULT)","Other Expenses(DEFAULT)");

    private CategoryDTO mapCategoryToDTO(MovementCategory category){
        CategoryDTO newDTO = new CategoryDTO();
        newDTO.setId(category.getId());
        newDTO.setName(category.getName());
        newDTO.setDescription(category.getDescription());
        newDTO.setFlagActive(category.getFlagActive());
        newDTO.setUserId(category.getUser().getId());
        newDTO.setFallbackCategoryId(category.getFallbackCategoryId());
        return newDTO;
    }
    private MovementCategory mapDtoToCategory(CategoryDTO dto){
        MovementCategory categoryEntity;

        if(dto.getId()!=null){
            categoryEntity = categoriesRepository.findById(dto.getId()).orElseThrow();
        }
        else {
            categoryEntity = new MovementCategory();
        }

        categoryEntity.setName(dto.getName());
        categoryEntity.setDescription(dto.getDescription());
        categoryEntity.setFlagActive(dto.getFlagActive());
        categoryEntity.setFallbackCategoryId(dto.getFallbackCategoryId());
        categoryEntity.setUser(userRepository.findById(dto.getUserId()).orElseThrow());

        return categoryEntity;
    }

    private void validateCategory(CategoryDTO categoryForValidation) throws InvalidCategoryException {

        if(categoryForValidation == null){
            throw new InvalidCategoryException("Category cannot be null");
        }
        if (categoryForValidation.getUserId() == null){
            throw new InvalidCategoryException("User cannot be null");
        }
        if (!userRepository.existsById(categoryForValidation.getUserId())){
            throw new InvalidCategoryException("User not found");
        }
        if(categoryForValidation.getName() == null || categoryForValidation.getName().isBlank()){
            throw new InvalidCategoryException("Name cannot be blank");
        }
    }

    @Override
    public List<CategoryDTO> getCategoriesForUser(Long userId) {
        return categoriesRepository
                .findAllByUserId(userId)
                .stream()
                .map(this::mapCategoryToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO getCategoryById(Long categoryId) throws NoSuchElementException {
        MovementCategory category = categoriesRepository.findById(categoryId).orElseThrow();
        return mapCategoryToDTO(category);
    }

    @Override
    public CategoryDTO saveCategory(CategoryDTO category) throws InvalidCategoryException {

        Boolean flagCategoryExists=false;
        if(category!=null){
            flagCategoryExists = category.getId()==null?false:true;
        }

        validateCategory(category);


        MovementCategory savedCategory = mapDtoToCategory(category);
        try {
            savedCategory = categoriesRepository.save(savedCategory);
            category.setId(savedCategory.getId());
        }
        catch (DataIntegrityViolationException e){
            //TODO handle the violated constraint specifically; rethrow the exception otherwise
            throw new InvalidCategoryException("Save would violate data integrity");
        }

        if(!flagCategoryExists){
            categoryChangeNotifier.notifyAllObservers(savedCategory, CrudChangeNotifier.NewState.CREATE);
        }
        else {
            categoryChangeNotifier.notifyAllObservers(savedCategory, CrudChangeNotifier.NewState.UPDATE);
        }

        return category;
    }

    @Override
    @Transactional
    public void deleteCategoryById(Long categoryId) throws IncorrectCategoryIdException {

        MovementCategory categoryToDelete;

        try {
            categoryToDelete = categoriesRepository.findById(categoryId).orElseThrow();
        }catch (NoSuchElementException e){
            throw new IncorrectCategoryIdException();
        }

        categoryChangeNotifier.notifyAllObservers(categoryToDelete, CrudChangeNotifier.NewState.DELETE);

        categoriesRepository.deleteById(categoryId);
    }

    @Override
    public List<CategoryDTO> getAll() {
        return categoriesRepository.findAll().stream().map(this::mapCategoryToDTO).collect(Collectors.toList());
    }

    @Override
    public void changeCategoryFlagActive(Long categoryId,Boolean flagActive) throws IncorrectCategoryIdException {

        MovementCategory categoryToModify;

        try {
            categoryToModify =  categoriesRepository.findById(categoryId).orElseThrow();
        }catch (NoSuchElementException e){
            throw new IncorrectCategoryIdException();
        }

        categoryToModify.setFlagActive(flagActive);
        categoriesRepository.save(categoryToModify);
        categoryChangeNotifier.notifyAllObservers(categoryToModify, CrudChangeNotifier.NewState.UPDATE);
    }

    @Override
    public List<CategoryDTO> getActiveCategoriesForUser(Long userId) {
        return categoriesRepository
                .findAllByUserId(userId)
                .stream()
                .filter(MovementCategory::getFlagActive)
                .map(this::mapCategoryToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDTO> getDefaultCategoriesForUser(Long userId) {
        return categoriesRepository.findAllByUserId(userId)
                .stream()
                .filter(category -> defaultCategoryNames.contains(category.getName()))
                .map(this::mapCategoryToDTO)
                .collect(Collectors.toList());
    }

    private MovementCategory createDefaultCategoryWithName(Long userId, String name) throws NoSuchElementException{
        MovementCategory toInsert = new MovementCategory();
        toInsert.setName(name);
        toInsert.setUser(userRepository.findById(userId).orElseThrow());
        toInsert.setFlagActive(true);

        return toInsert;
    }

    @Override
    public void createDefaultCategoriesForUser(Long userId) throws IncorrectUserIdException {
        if(!userRepository.existsById(userId))
            throw new IncorrectUserIdException();

        List<String> presentDefaultCategories = getDefaultCategoriesForUser(userId)
                .stream()
                .map(CategoryDTO::getName)
                .collect(Collectors.toList());

        List<MovementCategory> toInsert;
        try {
            toInsert = defaultCategoryNames.stream()
                    .filter(name -> !presentDefaultCategories.contains(name))
                    .map(name -> createDefaultCategoryWithName(userId,name))
                    .collect(Collectors.toList());
        }catch (NoSuchElementException e){
            throw new IncorrectUserIdException();
        }

        categoriesRepository.saveAll(toInsert);
        categoryChangeNotifier.notifyAllObservers(toInsert, CrudChangeNotifier.NewState.CREATE);
    }

    @Override
    public Boolean isCategoryDefault(String categoryName) {
        return defaultCategoryNames.contains(categoryName);
    }

    @Override
    public Boolean existsById(Long id) {
        return categoriesRepository.existsById(id);
    }
}
