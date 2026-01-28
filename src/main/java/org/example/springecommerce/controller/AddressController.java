package org.example.springecommerce.controller;

import jakarta.validation.Valid;
import org.example.shared.entityForm.AddressForm;
import org.example.shared.model.entity.Address;
import org.example.shared.repository.AddressRepository;
import org.example.shared.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@Controller
public class AddressController {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/user/address/add")
    public String addAddress(
            @Valid @ModelAttribute("addressForm") AddressForm form,
            BindingResult result,
            @AuthenticationPrincipal UserDetails currentUser,
            Model model
    ) {
        if (currentUser == null) {
            return "redirect:/login";
        }

        org.example.shared.model.entity.User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (result.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("addresses", addressRepository.findByUserAndIsActiveTrue(user));
            return "user/profile";
        }

        try {
            Address entity = new Address();
            entity.setStreet(form.getStreet());
            entity.setCity(form.getCity());
            entity.setZipCode(form.getZipCode());
            entity.setCountry(form.getCountry());
            entity.setIsActive(true);
            entity.setUser(user);

            addressRepository.save(entity);
        } catch (Exception e) {
            result.reject("globalError", "Erreur : " + e.getMessage());
            model.addAttribute("user", user);
            return "user/profile";
        }

        return "redirect:/user/profile?success=address";
    }


    @PostMapping("/user/address/delete")
    public String deleteAddress(@RequestParam Long addressId,
                                @AuthenticationPrincipal UserDetails currentUser) { // Utilise UserDetails

        if (currentUser == null) return "redirect:/login";

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Adresse introuvable"));


        if (!address.getUser().getEmail().equals(currentUser.getUsername())) {
            return "redirect:/user/profile?error=unauthorized";
        }

        address.setIsActive(false);
        addressRepository.save(address);

        return "redirect:/user/profile?success=deleted";
    }



    @GetMapping("/user/address/edit/{id}")
    public String showEditAddress(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails currentUser, // Utilise UserDetails ici
                                  Model model) {
        if (currentUser == null) return "redirect:/login";

        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Adresse introuvable"));

        if (!address.getUser().getEmail().equals(currentUser.getUsername())) {
            return "redirect:/user/profile?error=unauthorized";
        }

        model.addAttribute("addressForm", address);
        return "user/edit-address";
    }



    @PostMapping("/user/address/update")
    public String updateAddress(@Valid @ModelAttribute("addressForm") Address address,
                                BindingResult result,
                                @AuthenticationPrincipal UserDetails currentUser) {
        if (result.hasErrors()) return "user/edit-address";

        Address existing = addressRepository.findById(address.getId()).orElseThrow();

        if (!existing.getUser().getEmail().equals(currentUser.getUsername())) {
            return "redirect:/user/profile?error=unauthorized";
        }

        address.setUser(existing.getUser());
        address.setIsActive(true);

        addressRepository.save(address);
        return "redirect:/user/profile?success=updated";
    }
}
