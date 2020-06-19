package com.ryanev.personalfinancetracker.controllers;

import com.ryanev.personalfinancetracker.dto.MovementFormDTO;
import com.ryanev.personalfinancetracker.dto.MovementViewDTO;
import com.ryanev.personalfinancetracker.dto.implementations.DefaultMovementFormDTO;
import com.ryanev.personalfinancetracker.dto.implementations.MovementViewDtoAdapter;
import com.ryanev.personalfinancetracker.entities.Movement;
import com.ryanev.personalfinancetracker.entities.MovementCategory;
import com.ryanev.personalfinancetracker.services.CategoriesService;
import com.ryanev.personalfinancetracker.services.MovementsService;
import com.ryanev.personalfinancetracker.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.List;
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

    @GetMapping
    public String movementsLandingPage(Model model,
                                       @PathVariable("userId") Long userId){

        //ToDO build optional search params and pass to service
        //Todo understand Spring data specification interface https://spring.io/blog/2011/04/26/advanced-spring-data-jpa-specifications-and-querydsl/
        List<MovementViewDTO> movementsList = movementsService.getMovementsForUser(userId)
                .stream()
                .map(movement -> new MovementViewDtoAdapter(movement))
                .collect(Collectors.toList());

        model.addAttribute("baseUrl", buildControllerBaseURL(userId));
        model.addAttribute("movementsList",movementsList);

        return "movements/movements-list";
    }


    @GetMapping("/new")
    public String newMovementPage(Model model,
                                  @PathVariable("userId") long userId) {

        List<MovementCategory> categories = categoriesService.getCategoriesForUser(userId);

        model = loadMovementFormModel(model,userId,"New",new Movement(),categories);


        return "movements/movement-form";
    }

    @GetMapping("/update")
    public String editMovementPage(Model model,
                                   @PathVariable("userId") Long userId,
                                   @RequestParam("id") Long movementId ){

        List<MovementCategory> categories = categoriesService.getCategoriesForUser(userId);
        Movement movementForEdit = movementsService.getMovementById(movementId);

        model = loadMovementFormModel(model,userId,"Update",movementForEdit,categories);

        return "movements/movement-form";
    }

    @GetMapping("/delete")
    public String deleteMovementPage(Model model,
                                     @PathVariable("userId") Long userId,
                                     @RequestParam("id") Long movementId ){

        List<MovementCategory> categories = categoriesService.getCategoriesForUser(userId);
        Movement movementForDelete = movementsService.getMovementById(movementId);

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
            //todo add variable to make all form fields uneditable here attribute here
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
                                MovementFormDTO newMovementDTO,
                                @PathVariable("userId") Long userId) throws ParseException {

        //TODO this probably belongs to a builder, or some mapper class
        Movement newMovement = new Movement();
        newMovement.setId(newMovementDTO.getId());
        newMovement.setName(newMovementDTO.getName());
        newMovement.setDescription(newMovementDTO.getDescription());
        newMovement.setValueDate(newMovementDTO.getValueDate());
        newMovement.setUser(userService.getUserById(userId));
        newMovement.setCategory(categoriesService.getCategoryById(newMovementDTO.getCategoryId()));
        newMovement.setAmount( newMovementDTO.getUnsignedAmount() * (newMovementDTO.getFlagAmountPositive()?1:-1) );

        newMovement.setUser(userService.getUserById(userId)); //ToDo is this a good approach or just go with ids and rely on repo?
        movementsService.saveMovement(newMovement);

        return "redirect:"+buildControllerBaseURL(userId);
    }
    @PostMapping("/delete/confirm")
    public String deleteMovement(Model model,
                                 MovementFormDTO movementToDelete,
                                 @PathVariable("userId") Long userId){

        movementsService.deleteMovementById(movementToDelete.getId());

        return "redirect:"+buildControllerBaseURL(userId);
    }

}
