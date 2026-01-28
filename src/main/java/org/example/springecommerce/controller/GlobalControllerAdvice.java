package org.example.springecommerce.controller;

import jakarta.servlet.http.HttpSession;
import org.example.shared.model.DTO.CartItem;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice // nb art dispo sur ttes pages sans ajouter  a controller.
public class GlobalControllerAdvice {
    @ModelAttribute
    public void addCartData(HttpSession session, Model model) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");

        //badge Navbar
        int cartCount = (cart != null) ? cart.size() : 0;
        model.addAttribute("cartCount", cartCount);

        // stock en temps réel (Map ID -> Quantité)
        Map<Long, Integer> cartQuantities = new HashMap<>();
        if (cart != null) {
            cart.forEach((id, item) -> cartQuantities.put(id, item.getQuantity()));
        }
        model.addAttribute("cartQuantities", cartQuantities);
    }
}