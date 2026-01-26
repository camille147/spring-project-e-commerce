package org.example.springecommerce.controller;

import jakarta.validation.Valid;
import org.example.shared.entityForm.UserRegisterForm;
import org.example.shared.model.entity.User;
import org.example.shared.model.service.UserService;
import org.example.shared.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Controller
public class AuthController {

@Autowired
private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registerForm", new UserRegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("registerForm") UserRegisterForm form,
                               BindingResult result,
                               Model model) {


        if (result.hasErrors()) {
            return "register";
        }

        if (form.getBirthDate() != null) {
            long age = ChronoUnit.YEARS.between(form.getBirthDate(), LocalDate.now());
            if (age < 18) {
                result.rejectValue("birthDate", "error.birthDate", "Vous devez avoir au moins 18 ans pour vous inscrire.");
                return "register";
            }
        }

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "Les mots de passe ne correspondent pas");
            return "register";
        }

        if (userRepository.existsByEmail(form.getEmail())) {
            result.rejectValue("email", "error.email", "Cet email est déjà utilisé");
            return "register";
        }

        userService.registerUser(form);
        return "redirect:/login?registered=true";
    }


}
