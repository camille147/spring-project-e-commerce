package org.example.springecommerce.controller;

import org.example.shared.model.entity.Product;
import org.example.shared.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/user/product/{id}")
    public String getProductDetail(@PathVariable("id") Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        model.addAttribute("product", product);

        return "user/product-detail";
        // à rajouter aux btn @{/user/product/{id}(id=${p.id})}
    }



}
