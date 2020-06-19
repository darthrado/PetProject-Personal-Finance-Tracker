package com.ryanev.personalfinancetracker.controllers;

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
        String helloWorld = "Hello world";
        model.addAttribute("greeting",helloWorld);
        //TODO: redirect to authentication page or to user homepage if already authenticated

        return "home/home";
    }

    @GetMapping("/{user_id}/")
    public String displayIndividualHomepage(@PathVariable("user_id") int userId,  Model model){
        String greetUser = "Hello "+userId;

        model.addAttribute("greeting",greetUser);

        return "home/home";
    }

}
