package org.example.springecommerce.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.example.shared.entityForm.AddressForm;
import org.example.shared.model.entity.Order;
import org.example.shared.model.entity.User;
import org.example.shared.model.service.CustomUserDetails;
import org.example.shared.model.service.UserService;
import org.example.shared.repository.AddressRepository;
import org.example.shared.repository.OrderRepository;
import org.example.shared.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserService userService;


    @GetMapping("/user/profile")
    public String getProfile(@AuthenticationPrincipal UserDetails principal, Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);

        model.addAttribute("user", user);
        model.addAttribute("orders", orders);
        model.addAttribute("addressForm", new AddressForm());

        return "user/profile";
    }

    @PostMapping("/user/profile/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails principal,
                                @RequestParam String firstName,
                                @RequestParam String lastName) {
        userRepository.findByEmail(principal.getUsername()).ifPresent(user -> {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            userRepository.save(user);
        });

        return "redirect:/user/profile?success=profile";
    }

    @GetMapping("/user/export-data")
    public String exportUserData(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        model.addAttribute("user", user);
        model.addAttribute("orders", user.getOrders());
        model.addAttribute("addresses", user.getAddresses());

        return "user/data-export";
    }

    @PostMapping("/user/profile/delete-account")
    public String deleteAccount(Principal principal, HttpServletRequest request) throws ServletException {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        userService.deleteUserAccount(user.getId());

        request.logout();

        return "redirect:/login?deletedAccount=true";
    }

}