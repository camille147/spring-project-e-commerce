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

        // à rajouter aux btn @{/user/product/{id}(id=${p.id})}
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        int quantityInCart = 0;

        // 2. Si le produit est déjà dans le panier, on récupère sa quantité
        if (cart != null && cart.containsKey(id)) {
            quantityInCart = cart.get(id).getQuantity();
        }

        // 3. Calculer la quantité max que l'utilisateur peut encore ajouter
        int maxAvailable = product.getQuantity() - quantityInCart;

        model.addAttribute("product", product);
        model.addAttribute("maxAvailable", maxAvailable); // On envoie cette variable à la vue

        return "user/product-detail";
    }

}
