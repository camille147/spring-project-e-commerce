package org.example.springecommerce.controller;

import org.example.shared.model.entity.Product;
import org.example.shared.repository.CategoryRepository;
import org.example.shared.repository.ProductRepository;
import org.example.shared.repository.specification.ProductSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
public class ShopController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/user/shop")
    public String displayShop(Model model,
                              @RequestParam(name = "page", defaultValue = "0") int page,
                              @RequestParam(required = false) List<Long> categoryId,
                              @RequestParam(required = false) List<String> colors,
                              @RequestParam(required = false) Double price,
                              @RequestParam(defaultValue = "price,asc") String sort) {

        String[] sortParts = sort.split(",");
        Sort sortOrder = sortParts[1].equalsIgnoreCase("asc") ?
                Sort.by(sortParts[0]).ascending() :
                Sort.by(sortParts[0]).descending();

        Pageable pageable = PageRequest.of(page, 9, sortOrder);

        Specification<Product> spec = ProductSpecification.filterProducts(categoryId, colors, price);
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        model.addAttribute("product", productPage.getContent());
        model.addAttribute("selectedColors", colors);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("availableColors", productRepository.findAllDistinctColors());


        return "user/shop";

    }
}
