package org.example.springecommerce.controller;

import org.example.shared.model.entity.User;
import org.example.shared.model.service.UserService;
import org.example.shared.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {


    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user) {
        userService.registerUser(user);
        return "redirect:/login?registered=true";
    }

    @GetMapping("/admin/dashboard")
    public String adminHome() {
        return "admin/dashboard";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

}
