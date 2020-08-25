package com.ryanev.personalfinancetracker.web.controllers;

import com.ryanev.personalfinancetracker.services.dto.categories.CategoryDTO;
import com.ryanev.personalfinancetracker.services.dto.movements.MovementDTO;
import com.ryanev.personalfinancetracker.web.dto.movements.MovementFormDTO;
import com.ryanev.personalfinancetracker.web.dto.movements.MovementViewDTO;
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
import org.springframework.web.util.UriComponentsBuilder;

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

    private MovementViewDTO mapMovementToViewDTO(MovementDTO movement){
        MovementViewDTO newDTO = new MovementViewDTO();
        newDTO.setName(movement.getName());
        newDTO.setCategoryName(movement.getCategory());
        newDTO.setValueDate(movement.getValueDate());
        newDTO.setSignedAmount(movement.getAmount());

        String baseUri = buildControllerBaseURL(movement.getUserId());
        String updateLink = UriComponentsBuilder.fromUriString(baseUri.concat("/update"))
                .queryParam("id",movement.getId()).build().toUriString();

        String deleteLink = UriComponentsBuilder.fromUriString(baseUri.concat("/delete"))
                .queryParam("id",movement.getId()).build().toUriString();

        newDTO.setUpdateLink(updateLink);
        newDTO.setDeleteLink(deleteLink);

        return newDTO;
    }

    private MovementFormDTO mapMovementToFormDTO(MovementDTO movement){
        MovementFormDTO formDTO = new MovementFormDTO();
        if (movement.getAmount() != null) {
            formDTO.setUnsignedAmount(Math.abs(movement.getAmount()));
            formDTO.setFlagAmountPositive(movement.getAmount() > 0);
        }
        formDTO.setId(movement.getId());
        formDTO.setName(movement.getName());
        formDTO.setValueDate(movement.getValueDate());
        formDTO.setCategoryName(movement.getCategory());
        return formDTO;
    }

    private MovementDTO mapDtoToMovement(Long userId, MovementFormDTO dto){
        MovementDTO serviceDTO;
        if(dto.getId()!=null){
            serviceDTO = movementsService.getMovementById(dto.getId());
        }
        else {
            serviceDTO = new MovementDTO();
            serviceDTO.setUserId(userId);
        }

        serviceDTO.setName(dto.getName());
        serviceDTO.setDescription(dto.getDescription());
        serviceDTO.setCategory(dto.getCategoryName());
        serviceDTO.setAmount( dto.getUnsignedAmount() * (dto.getFlagAmountPositive()?1:-1) );
        serviceDTO.setValueDate(dto.getValueDate());
        return serviceDTO;
    }


    @GetMapping
    public String movementsLandingPage(Model model,
                                       @PathVariable("userId") Long userId) throws IncorrectUserIdException{

        if(!userService.existsById(userId)){
            throw new IncorrectUserIdException();
        }

        //ToDO build optional search params and pass to service
        //Todo understand Spring data specification interface https://spring.io/blog/2011/04/26/advanced-spring-data-jpa-specifications-and-querydsl/
        List<MovementViewDTO> movementsList = movementsService.getMovementsForUser(userId)
                .stream()
                .map(this::mapMovementToViewDTO)
                .sorted(Comparator.comparing(MovementViewDTO::getValueDate).reversed())
                .collect(Collectors.toList());

        model.addAttribute("baseUrl", buildControllerBaseURL(userId));
        model.addAttribute("movementsList",movementsList);

        return "movements/movements-list";
    }


    @GetMapping("/new")
    public String newMovementPage(Model model,
                                  @PathVariable("userId") long userId) throws IncorrectUserIdException {

        if(!userService.existsById(userId)){
            throw new IncorrectUserIdException();
        }

        List<CategoryDTO> categories = categoriesService.getActiveCategoriesForUser(userId);

        loadMovementFormModel(model,userId,"New",new MovementDTO(),categories);


        return "movements/movement-form";
    }

    @GetMapping("/update")
    public String editMovementPage(Model model,
                                   @PathVariable("userId") Long userId,
                                   @RequestParam("id") Long movementId ) throws IncorrectUserIdException, IncorrectMovementIdException {

        if(!userService.existsById(userId)){
            throw new IncorrectUserIdException();
        }

        MovementDTO movementForEdit;
        try {
            movementForEdit = movementsService.getMovementById(movementId);
        }
        catch (NoSuchElementException e){
            throw new IncorrectMovementIdException();
        }

        List<CategoryDTO> categories = categoriesService.getActiveCategoriesForUser(userId);

        loadMovementFormModel(model,userId,"Update",movementForEdit,categories);

        return "movements/movement-form";
    }

    @GetMapping("/delete")
    public String deleteMovementPage(Model model,
                                     @PathVariable("userId") Long userId,
                                     @RequestParam("id") Long movementId ) throws IncorrectUserIdException, IncorrectMovementIdException {

        if(!userService.existsById(userId)){
            throw new IncorrectUserIdException();
        }

        MovementDTO movementForDelete;
        try {
            movementForDelete = movementsService.getMovementById(movementId);
        }
        catch (NoSuchElementException e){
            throw new IncorrectMovementIdException();
        }

        List<CategoryDTO> categories = categoriesService.getActiveCategoriesForUser(userId);

        loadMovementFormModel(model,userId,"Delete",movementForDelete,categories);

        return "movements/movement-form";
    }

    private Model loadMovementFormModel(Model model, Long userId, String action, MovementDTO movement, List<CategoryDTO> categories){

        String baseUrl = buildControllerBaseURL(userId);
        String okButtonUrl;
        String formMethod;
        MovementFormDTO movementFormEntry = mapMovementToFormDTO(movement);

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

        if(!userService.existsById(userId)){
            throw new IncorrectUserIdException();
        }


        //TODO: I don't like this band aid fix. Add a validator for the DTO
        if (newMovementDTO.getUnsignedAmount() == null || newMovementDTO.getFlagAmountPositive() == null)
            throw new InvalidMovementException("Amount is null or Flag Amount is null");
        if(newMovementDTO.getUnsignedAmount() < 0)
            throw new InvalidMovementException("Amount can't be negative");

        MovementDTO movement = mapDtoToMovement(userId,newMovementDTO);

        movementsService.saveMovement(movement);

        return "redirect:"+buildControllerBaseURL(userId);
    }
    @PostMapping("/delete/confirm")
    public String deleteMovement(Model model,
                                 MovementFormDTO movementToDelete,
                                 @PathVariable("userId") Long userId) throws IncorrectUserIdException {

        if(!userService.existsById(userId)){
            throw new IncorrectUserIdException();
        }

        movementsService.deleteMovementById(movementToDelete.getId());

        return "redirect:"+buildControllerBaseURL(userId);
    }

}
