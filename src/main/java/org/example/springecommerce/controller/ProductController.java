package org.example.springecommerce.controller;

import jakarta.servlet.http.HttpSession;
import org.example.shared.model.DTO.CartItem;
import org.example.shared.model.entity.Product;
import org.example.shared.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/user/product/{id}")
    public String getProductDetail(@PathVariable("id") Long id, Model model, HttpSession session) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        if (product.getIsEnabled() == null || !product.getIsEnabled()) {
            throw new RuntimeException("Ce produit n'est plus disponible.");
        }

        @SuppressWarnings("unchecked")
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");

        int quantityInCart = 0;
        if (cart != null && cart.containsKey(id)) {
            quantityInCart = cart.get(id).getQuantity();
        }

        int maxAvailable = product.getQuantity() - quantityInCart;
        if (maxAvailable < 0) maxAvailable = 0; // Sécurité visuelle

        model.addAttribute("product", product);
        model.addAttribute("maxAvailable", maxAvailable);
        model.addAttribute("quantityInCart", quantityInCart);

        return "user/product-detail";
    }
}