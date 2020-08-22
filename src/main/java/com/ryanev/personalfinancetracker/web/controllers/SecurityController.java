package com.ryanev.personalfinancetracker.web.controllers;

import com.ryanev.personalfinancetracker.exceptions.UserAlreadyExistsException;
import com.ryanev.personalfinancetracker.services.users.UserService;
import com.ryanev.personalfinancetracker.web.dto.security.UserAccountDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SecurityController {

    @Autowired
    BCryptPasswordEncoder encoder;

    @Autowired
    UserService userService;

    @GetMapping("/register")
    public String register(Model model){
        UserAccountDTO userAccount = new UserAccountDTO();

        model.addAttribute("userAccount",userAccount);

        return "security/register";
    }
    @PostMapping("/register/save")
    public String registerSave(Model model, UserAccountDTO userAccount){
        userAccount.setPassword(encoder.encode(userAccount.getPassword()));

        try {
            userService.register(userAccount);
        }catch (UserAlreadyExistsException e){
            return "redirect:/register";
        }


        return "redirect:/";

    }

}
