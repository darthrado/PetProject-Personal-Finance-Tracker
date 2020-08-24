package com.ryanev.personalfinancetracker.web.controllers;

import com.ryanev.personalfinancetracker.web.dto.movements.MovementFormDTO;
import com.ryanev.personalfinancetracker.web.dto.movements.MovementViewDTO;
import com.ryanev.personalfinancetracker.web.dto.movements.implementations.DefaultMovementFormDTO;
import com.ryanev.personalfinancetracker.web.dto.movements.implementations.MovementViewDtoAdapter;
import com.ryanev.personalfinancetracker.data.entities.Movement;
import com.ryanev.personalfinancetracker.data.entities.MovementCategory;
import com.ryanev.personalfinancetracker.data.entities.User;
import com.ryanev.personalfinancetracker.exceptions.IncorrectCategoryIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectMovementIdException;
import com.ryanev.personalfinancetracker.exceptions.IncorrectUserIdException;
import com.ryanev.personalfinancetracker.exceptions.InvalidMovementException;
import com.ryanev.personalfinancetracker.services.categories.CategoriesService;
import com.ryanev.personalfinancetracker.services.movements.MovementsService;
import com.ryanev.personalfinancetracker.services.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/{userId}/movements")
public class MovementsController {

    @Autowired
    private UserService userService;
    @Autowired
    private MovementsService movementsService;
    @Autowired
    private CategoriesService categoriesService;

    private static final String controllerPath  = "movements";
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
    public String movementsLandingPage(Model model,
                                       @PathVariable("userId") Long userId) throws IncorrectUserIdException{

        if(!checkIfUserExists(userId)){
            throw new IncorrectUserIdException();
        }

        //ToDO build optional search params and pass to service
        //Todo understand Spring data specification interface https://spring.io/blog/2011/04/26/advanced-spring-data-jpa-specifications-and-querydsl/
        List<MovementViewDTO> movementsList = movementsService.getMovementsForUser(userId)
                .stream()
                .map(MovementViewDtoAdapter::new)
                .sorted(Comparator.comparing(MovementViewDtoAdapter::getValueDate).reversed())
                .collect(Collectors.toList());

        model.addAttribute("baseUrl", buildControllerBaseURL(userId));
        model.addAttribute("movementsList",movementsList);

        return "movements/movements-list";
    }


    @GetMapping("/new")
    public String newMovementPage(Model model,
                                  @PathVariable("userId") long userId) throws IncorrectUserIdException {

        if(!checkIfUserExists(userId)){
            throw new IncorrectUserIdException();
        }

        List<MovementCategory> categories = categoriesService.getActiveCategoriesForUser(userId);

        model = loadMovementFormModel(model,userId,"New",new Movement(),categories);


        return "movements/movement-form";
    }

    @GetMapping("/update")
    public String editMovementPage(Model model,
                                   @PathVariable("userId") Long userId,
                                   @RequestParam("id") Long movementId ) throws IncorrectUserIdException, IncorrectMovementIdException {

        if(!checkIfUserExists(userId)){
            throw new IncorrectUserIdException();
        }

        Movement movementForEdit;
        try {
            movementForEdit = movementsService.getMovementById(movementId);
        }
        catch (NoSuchElementException e){
            throw new IncorrectMovementIdException();
        }

        List<MovementCategory> categories = categoriesService.getActiveCategoriesForUser(userId);

        model = loadMovementFormModel(model,userId,"Update",movementForEdit,categories);

        return "movements/movement-form";
    }

    @GetMapping("/delete")
    public String deleteMovementPage(Model model,
                                     @PathVariable("userId") Long userId,
                                     @RequestParam("id") Long movementId ) throws IncorrectUserIdException, IncorrectMovementIdException {

        if(!checkIfUserExists(userId)){
            throw new IncorrectUserIdException();
        }

        Movement movementForDelete;
        try {
            movementForDelete = movementsService.getMovementById(movementId);
        }
        catch (NoSuchElementException e){
            throw new IncorrectMovementIdException();
        }

        List<MovementCategory> categories = categoriesService.getActiveCategoriesForUser(userId);

        model = loadMovementFormModel(model,userId,"Delete",movementForDelete,categories);

        return "movements/movement-form";
    }

    private Model loadMovementFormModel(Model model, Long userId, String action, Movement movement, List<MovementCategory> categories){

        String baseUrl = buildControllerBaseURL(userId);
        String okButtonUrl;
        String formMethod;
        MovementFormDTO movementFormEntry = new DefaultMovementFormDTO(movement);

        boolean disableFormFields;
        if (action.equals("Delete")){
            okButtonUrl = new StringBuilder(baseUrl).append(slash).append("delete").append(slash).append("confirm").toString();
            formMethod = RequestMethod.DELETE.name();
            disableFormFields = true;
        }
        else {
            okButtonUrl = new StringBuilder(baseUrl).append(slash).append("save").toString();
            formMethod = RequestMethod.POST.name();
            disableFormFields = false;
        }

        model.addAttribute("okButtonText",action);
        model.addAttribute("okButtonUrl",okButtonUrl);
        model.addAttribute("disableFormFields",disableFormFields);
        model.addAttribute("backButtonUrl",baseUrl);

        model.addAttribute("formMethod",formMethod);
        model.addAttribute("action",action);
        model.addAttribute("baseUrl",baseUrl);
        model.addAttribute("movement",movementFormEntry);
        model.addAttribute("uid",userId);
        model.addAttribute("categories",categories);

        return model;
    }


    @PostMapping("/save")
    public  String saveMovement(Model model,
                                @Valid
                                @ModelAttribute("movement")    MovementFormDTO newMovementDTO,
                                @PathVariable("userId") Long userId) throws IncorrectUserIdException, InvalidMovementException, IncorrectCategoryIdException {

        if(!checkIfUserExists(userId)){
            throw new IncorrectUserIdException();
        }

        //TODO this probably belongs to a builder, or some mapper class
        Movement newMovement;
        if (newMovementDTO.getId() != null){
            newMovement=movementsService.getMovementById(newMovementDTO.getId());
        }else {
            newMovement = new Movement();
        }
        newMovement.setName(newMovementDTO.getName());
        newMovement.setDescription(newMovementDTO.getDescription());
        newMovement.setValueDate(newMovementDTO.getValueDate());
        newMovement.setUser(userService.getUserById(userId));

        try {
            newMovement.setCategory(categoriesService.getCategoryById(newMovementDTO.getCategoryId()));
        }catch (NoSuchElementException e){
            throw new IncorrectCategoryIdException();
        }


        //TODO: I don't like this band aid fix. Add a validator for the DTO
        if (newMovementDTO.getUnsignedAmount() == null || newMovementDTO.getFlagAmountPositive() == null)
            throw new InvalidMovementException("Amount is null or Flag Amount is null");
        if(newMovementDTO.getUnsignedAmount() < 0)
            throw new InvalidMovementException("Amount can't be negative");
        newMovement.setAmount( newMovementDTO.getUnsignedAmount() * (newMovementDTO.getFlagAmountPositive()?1:-1) );

        movementsService.saveMovement(newMovement);

        return "redirect:"+buildControllerBaseURL(userId);
    }
    @PostMapping("/delete/confirm")
    public String deleteMovement(Model model,
                                 MovementFormDTO movementToDelete,
                                 @PathVariable("userId") Long userId) throws IncorrectUserIdException {

        if(!checkIfUserExists(userId)){
            throw new IncorrectUserIdException();
        }

        movementsService.deleteMovementById(movementToDelete.getId());

        return "redirect:"+buildControllerBaseURL(userId);
    }

}
