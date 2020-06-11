package com.ryanev.personalfinancetracker.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private Environment env;

    @GetMapping("/")
    public String displayHomepage(Model model){
        String helloWorld = "Hello world";
        model.addAttribute("greeting",helloWorld);

        return "home/home";
    }

}
