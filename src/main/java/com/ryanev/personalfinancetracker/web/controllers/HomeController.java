package com.ryanev.personalfinancetracker.web.controllers;

import com.ryanev.personalfinancetracker.web.dto.UserNavbarDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    @Autowired
    private Environment env;

    @GetMapping("/")
    public String displayHomepage(Model model){

        if(model.containsAttribute("userNav") && model.getAttribute("userNav") instanceof  UserNavbarDTO){
            UserNavbarDTO navbarDTO = (UserNavbarDTO)model.getAttribute("userNav");
            if (navbarDTO.getId()!=null){
                return "redirect:/"+navbarDTO.getId()+"/";
            }
        }
        //TODO: not very clean but works for now. See if I can improve on this

        return "home/home";
    }

    @GetMapping("/{user_id}/")
    public String displayIndividualHomepage(@PathVariable("user_id") int userId,  Model model){

        model.addAttribute("userId",userId);

        return "home/home";
    }

}
