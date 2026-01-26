package org.example.springecommerce.controller;

import jakarta.validation.Valid;
import org.example.shared.entityForm.AddressForm;
import org.example.shared.model.entity.Address;
import org.example.shared.model.service.CustomUserDetails;
import org.example.shared.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AddressController {

    @Autowired
    private AddressRepository addressRepository;

    @PostMapping("/user/address/add")
    public String addAddress(
            @Valid @ModelAttribute("addressForm") AddressForm form,
            BindingResult result,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("user", currentUser.getUser());
            model.addAttribute("userId", currentUser.getId());
            return "user/profile";
        }
        try {
            // Convers° Form -> Entity
            Address entity = new Address();
            entity.setStreet(form.getStreet());
            entity.setCity(form.getCity());
            entity.setZipCode(form.getZipCode());
            entity.setCountry(form.getCountry());
            entity.setIsActive(true);

            // 3. Liaison avec l'utilisateur connecté
            entity.setUser(currentUser.getUser());

            // 4. Sauvegarde
            addressRepository.save(entity);
        } catch (Exception e) {
            result.reject("globalError", "Erreur lors de la sauvegarde : " + e.getMessage());
            model.addAttribute("user", currentUser.getUser());
            return "user/profile";
        }
        return "redirect:/user/profile";
    }
}
