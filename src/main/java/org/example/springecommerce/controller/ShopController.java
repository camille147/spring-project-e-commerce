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
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) List<Long> categoryId,
                              @RequestParam(required = false) List<String> colors,
                              @RequestParam(required = false) Double price,
                              @RequestParam(defaultValue = "price,asc") String sort) {

        String[] sortParts = sort.split(",");
        Sort sortOrder = sortParts[1].equalsIgnoreCase("asc") ?
                Sort.by(sortParts[0]).ascending() :
                Sort.by(sortParts[0]).descending();

        Pageable pageable = PageRequest.of(page, 12, sortOrder);

        Specification<Product> spec = ProductSpecification.filterProducts(keyword, categoryId, colors, price);
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        int totalPages = productPage.getTotalPages();
        int currentPage = page;
        int radius = 2;

        int startPage = Math.max(0, currentPage - radius);
        int endPage = Math.min(totalPages - 1, currentPage + radius);

        if (startPage == 0) {
            endPage = Math.min(totalPages - 1, startPage + (radius * 2));
        }
        if (endPage == totalPages - 1) {
            startPage = Math.max(0, endPage - (radius * 2));
        }

        model.addAttribute("product", productPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("selectedColors", colors);
        model.addAttribute("categoryId", categoryId);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("availableColors", productRepository.findAllDistinctColors());

        model.addAttribute("brands", productRepository.findAllDistinctBrands());

        return "user/shop";
    }
}
