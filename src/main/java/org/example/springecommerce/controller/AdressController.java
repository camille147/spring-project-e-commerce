package org.example.springecommerce.controller;

import jakarta.validation.Valid;
import org.example.shared.entityForm.AdressForm;
import org.example.shared.model.entity.Address;
import org.example.shared.model.service.CustomUserDetails;
import org.example.shared.repository.AddressRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AdressController {

    private AddressRepository addressRepository;

    @PostMapping("/user/address/add")
    public String addAddress(@Valid @ModelAttribute("addressForm") AdressForm form,
                             BindingResult result,
                             @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (result.hasErrors()) {
            return "user/profile";
        }

        // convers° Form -> Entity
        Address entity = new Address();
        entity.setStreet(form.getStreet());
        entity.setCity(form.getCity());
        entity.setZipCode(form.getZipCode());
        entity.setCountry(form.getCountry());
        entity.setUser(currentUser.getUser()); // liaison adress -> user connecté


        addressRepository.save(entity);
        return "redirect:/user/profile";
    }
}
