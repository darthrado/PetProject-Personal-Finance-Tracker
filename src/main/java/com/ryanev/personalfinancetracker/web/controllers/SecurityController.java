package com.ryanev.personalfinancetracker.web.controllers;

import com.ryanev.personalfinancetracker.exceptions.UserAlreadyExistsException;
import com.ryanev.personalfinancetracker.services.users.UserService;
import com.ryanev.personalfinancetracker.web.dto.security.UserAccountDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
public class SecurityController {

    @Autowired
    BCryptPasswordEncoder encoder;

    @Autowired
    UserService userService;

    @GetMapping("/register")
    public String register(Model model){

        if(!model.containsAttribute("userAccount")){
            UserAccountDTO userAccount = new UserAccountDTO();
            model.addAttribute("userAccount",userAccount);
        }

        return "security/register";
    }
    @PostMapping("/register/save")
    public String registerSave(Model model,
                               @Valid @ModelAttribute("userAccount") UserAccountDTO userAccount,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes){


        if(userService.existsByUsername(userAccount.getUsername())){
            FieldError fieldError = new FieldError("userAccount", "username","must be unique");
            bindingResult.addError(fieldError);
        }

        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("userAccount",userAccount);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userAccount",bindingResult);
            return "redirect:/register";
        }

        userAccount.setPassword(encoder.encode(userAccount.getPassword()));

        try {
            userService.register(userAccount);
        }catch (UserAlreadyExistsException e){
            return "redirect:/register";
        }

        return "redirect:/";

    }

}
