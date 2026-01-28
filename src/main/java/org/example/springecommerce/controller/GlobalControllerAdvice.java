package org.example.springecommerce.controller;

import jakarta.servlet.http.HttpSession;
import org.example.shared.model.DTO.CartItem;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Map;

@ControllerAdvice // nb art dispo sur ttes pages sans ajouter  a controller.
public class GlobalControllerAdvice {
    @ModelAttribute("cartCount")
    public int getCartCount(HttpSession session) {
        Map<?, ?> cart = (Map<?, ?>) session.getAttribute("cart");
        return (cart != null) ? cart.size() : 0; //compte nb art dif
    }
}