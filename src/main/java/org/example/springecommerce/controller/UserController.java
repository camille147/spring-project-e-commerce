package org.example.springecommerce.controller;

import org.example.shared.entityForm.AddressForm;
import org.example.shared.model.entity.User;
import org.example.shared.model.service.CustomUserDetails;
import org.example.shared.repository.AddressRepository;
import org.example.shared.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;


    @GetMapping("/user/profile")
    public String getProfile(@AuthenticationPrincipal UserDetails principal, Model model) {

        if (principal == null) { //session expirée ou accès direct
            return "redirect:/login";
        }

        // CustomUserDetails
        Long userId;
        if (principal instanceof CustomUserDetails customUser) {
            userId = customUser.getId();
        } else {
            userId = userRepository.findByEmail(principal.getUsername())
                    .map(User::getId)
                    .orElse(null);
        }

        if (userId != null) {
            // rechargement du user dpusi le repo -> recup des adresses
            userRepository.findById(userId).ifPresent(userInDb -> {
                model.addAttribute("user", userInDb);
                model.addAttribute("userId", userInDb.getId());
                model.addAttribute("userEmail", userInDb.getEmail());
            });

            model.addAttribute("addressForm", new AddressForm());
        }

        return "user/profile";
    }


}