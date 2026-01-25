package org.example.springecommerce.controller;

import org.example.shared.model.entity.Product;
import org.example.shared.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ShopController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/user/shop")
    public String displayShop(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("product", products);
        return "user/shop";
    }


}
