package org.example.springecommerce.controller;

import jakarta.validation.Valid;
import org.example.shared.entityForm.AddressForm;
import org.example.shared.model.entity.Address;
import org.example.shared.model.entity.User;
import org.example.shared.model.service.AddressService;
import org.example.shared.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/add")
    public String addAddress(@Valid @ModelAttribute("addressForm") AddressForm form,
                             BindingResult result,
                             @AuthenticationPrincipal UserDetails currentUser,
                             Model model) {

        User user = userRepository.findByEmail(currentUser.getUsername()).orElseThrow();

        if (result.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("addresses", addressService.findActiveByUser(user));
            return "user/profile";
        }

        try {
            addressService.createAddress(form, user);
            return "redirect:/user/profile?success=address";
        } catch (Exception e) {
            return "redirect:/user/profile?error=system";
        }
    }

    @PostMapping("/delete")
    public String deleteAddress(@RequestParam Long addressId,
                                @AuthenticationPrincipal UserDetails currentUser) {
        try {
            addressService.softDelete(addressId, currentUser.getUsername());
            return "redirect:/user/profile?success=deleted";
        } catch (SecurityException e) {
            return "redirect:/user/profile?error=unauthorized";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditAddress(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails currentUser,
                                  Model model) {
        try {
            Address address = addressService.findById(id);
            if (!address.getUser().getEmail().equals(currentUser.getUsername())) {
                return "redirect:/user/profile?error=unauthorized";
            }
            model.addAttribute("addressForm", address);
            return "user/edit-address";
        } catch (Exception e) {
            return "redirect:/user/profile?error=notfound";
        }
    }

    @PostMapping("/update")
    public String updateAddress(@Valid @ModelAttribute("addressForm") Address address,
                                BindingResult result,
                                @AuthenticationPrincipal UserDetails currentUser) {
        if (result.hasErrors()) return "user/edit-address";

        try {
            addressService.updateAddress(address, currentUser.getUsername());
            return "redirect:/user/profile?success=updated";
        } catch (SecurityException e) {
            return "redirect:/user/profile?error=unauthorized";
        }
    }
}