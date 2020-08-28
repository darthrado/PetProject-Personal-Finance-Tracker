package com.ryanev.personalfinancetracker.web.advice;

import com.ryanev.personalfinancetracker.data.entities.User;
import com.ryanev.personalfinancetracker.exceptions.UnauthorizedToAccessContent;
import com.ryanev.personalfinancetracker.services.users.UserService;
import com.ryanev.personalfinancetracker.web.dto.UserNavbarDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@ControllerAdvice
public class NavbarControllerAdvice {

    @Autowired
    UserService userService;

    @ModelAttribute
    public void injectParent(@Nullable @PathVariable("userId") Long userId, Model model) throws UnauthorizedToAccessContent {

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        String username = authentication.getName();

        if(!username.equals("anonymousUser")){ //TODO i don't like this. there can be a user with "anonymousUser"
            UserNavbarDTO userDTO = new UserNavbarDTO();
            User user = userService.getUserByUsername(username);
            userDTO.setId(user.getId());
            userDTO.setUsername(user.getUsername());

            if(userId!=null&&!userId.equals(user.getId())){
                throw new UnauthorizedToAccessContent();
            }

            model.addAttribute("userNav", userDTO);
        }
    }


}
