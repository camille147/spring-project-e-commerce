package org.example.springecommerce.controller;

import jakarta.transaction.Transactional;
import org.example.shared.model.entity.User;
import org.example.shared.model.service.CustomUserDetails;
import org.example.shared.model.service.CustomUserDetailsService;
import org.example.shared.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;


    @GetMapping("/user/profile")
    public String getProfile(@AuthenticationPrincipal UserDetails principal, Model model) {

        if (principal == null) { //session expirée ou accès direct
            return "redirect:/login";
        }

        // CustomUserDetails
        if (principal instanceof CustomUserDetails customUser) {
            model.addAttribute("userId", customUser.getId());
            // On peut aussi passer l'email ou le nom
            model.addAttribute("user", customUser.getUser());
            model.addAttribute("userEmail", customUser.getUsername());
        }
        // si c User et pas CUD -> recup id via email
        else {
            userRepository.findByEmail(principal.getUsername())
                    .ifPresent(u -> model.addAttribute("userId", u.getId()));
        }

        return "user/profile";
    }
}