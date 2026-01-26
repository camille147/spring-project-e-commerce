package org.example.springecommerce.controller;

import org.example.shared.model.entity.Product;
import org.example.shared.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ShopController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/user/shop")
    public String displayShop(Model model,
                              @RequestParam(name = "page", defaultValue = "0") int page) {

        // 1. Créer la demande de page : (page actuelle, nombre d'éléments par page)
        // Ici on met 6 car vous avez une grille de 3 colonnes (3x2 = 6, c'est joli)
        Pageable pageable = PageRequest.of(page, 6);

        // 2. Récupérer la PAGE de produits (et non plus la liste complète)
        Page<Product> productPage = productRepository.findAll(pageable);

        // 3. Envoyer les infos à la vue
        model.addAttribute("product", productPage.getContent()); // La liste réduite des produits
        model.addAttribute("currentPage", page);                  // La page actuelle (0, 1, 2...)
        model.addAttribute("totalPages", productPage.getTotalPages()); // Le nombre total de pages

        return "user/shop"; // Pas besoin de .html à la fin dans le return avec Thymeleaf
    }
}