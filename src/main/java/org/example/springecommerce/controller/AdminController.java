package org.example.springecommerce.controller;

import org.example.shared.entityForm.ProductForm;
import org.example.shared.model.entity.Category;
import org.example.shared.model.entity.Product;
import org.example.shared.repository.CategoryRepository;
import org.example.shared.repository.OrderRepository;
import org.example.shared.repository.ProductRepository;
import org.example.springecommerce.controller.dto.OrderCountByDayDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class AdminController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/admin/dashboard")
    public String adminHome() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/discounts")
    public String adminDiscounts() {
        return "admin/discounts";
    }


    @GetMapping("/admin/orders")
    public String adminOrders() {
        return "admin/orders";
    }

    @GetMapping("/admin/products")
    public String adminProducts(Model model,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<Product> pageProducts = productRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
        model.addAttribute("products", pageProducts.getContent());
        model.addAttribute("currentPage", pageProducts.getNumber());
        model.addAttribute("totalPages", pageProducts.getTotalPages());
        model.addAttribute("pageSize", pageProducts.getSize());
        model.addAttribute("totalItems", pageProducts.getTotalElements());
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/products";
    }

    @GetMapping("/admin/products/{id}/data")
    public ResponseEntity<ProductForm> getProductData(@PathVariable("id") Long id) {
        Optional<Product> opt = productRepository.findById(id);
        if (opt.isPresent()) {
            Product p = opt.get();
            ProductForm form = new ProductForm();
            form.setId(p.getId());
            form.setProductName(p.getProductName());
            form.setBrand(p.getBrand());
            form.setPrice(p.getPrice());
            form.setQuantity(p.getQuantity());
            form.setDescription(p.getDescription());
            form.setReference(p.getReference());
            form.setCategoryId(p.getCategory() != null ? p.getCategory().getId() : null);
            return ResponseEntity.ok(form);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/admin/products/edit")
    public String editProductForm(@RequestParam(value = "id", required = false) Long id, Model model) {
        ProductForm form = new ProductForm();
        if (id != null) {
            Optional<Product> opt = productRepository.findById(id);
            if (opt.isPresent()) {
                Product p = opt.get();
                form.setId(p.getId());
                form.setProductName(p.getProductName());
                form.setBrand(p.getBrand());
                form.setPrice(p.getPrice());
                form.setQuantity(p.getQuantity());
                form.setDescription(p.getDescription());
                form.setReference(p.getReference());
                form.setCategoryId(p.getCategory() != null ? p.getCategory().getId() : null);
            }
        }
        model.addAttribute("productForm", form);
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/product-edit";
    }

    @PostMapping("/admin/products/edit")
    public String saveProduct(@Valid @ModelAttribute("productForm") ProductForm form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            return "admin/product-edit";
        }

        Product p;
        if (form.getId() != null) {
            p = productRepository.findById(form.getId()).orElse(new Product());
        } else {
            p = new Product();
        }

        p.setProductName(form.getProductName());
        p.setBrand(form.getBrand());
        p.setPrice(form.getPrice());
        p.setQuantity(form.getQuantity());
        p.setDescription(form.getDescription());
        p.setReference(form.getReference());

        if (form.getCategoryId() != null) {
            Category c = categoryRepository.findById(form.getCategoryId()).orElse(null);
            p.setCategory(c);
        }

        productRepository.save(p);
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "10") int size) {
        productRepository.deleteById(id);
        return "redirect:/admin/products?page=" + page + "&size=" + size;
    }

    @GetMapping("/admin/users")
    public String adminUsers() {
        return "admin/users";
    }

    @GetMapping("/admin/bestProduct")
    @ResponseBody
    public ResponseEntity<List<Product>> bestProduct(){
        List<Product> best = productRepository.findBestProducts();
        return ResponseEntity.ok(best);
    }

    @GetMapping("/admin/products/search")
    public String searchProduct(Model model, @RequestParam(value = "search", required = false) String search) {
        model.addAttribute("search", search);
        return "admin/products";
    }

    @GetMapping("/admin/dashboard/stats")
    @ResponseBody
    public ResponseEntity<List<OrderCountByDayDto>> ordersStats(@RequestParam(value = "month", required = false) Integer month,
                                                                 @RequestParam(value = "year", required = false) Integer year) {
        LocalDate now = LocalDate.now();
        int m = (month != null) ? month : now.getMonthValue();
        int y = (year != null) ? year : now.getYear();

        List<Object[]> raw = orderRepository.findOrderCountByMonthYear(m, y);Map<Integer, Long> counts = new HashMap<>();
        for (Object[] row : raw) {
            Number dayNum = (Number) row[0];
            Number cntNum = (Number) row[1];
            int d = dayNum.intValue();
            long c = cntNum.longValue();
            counts.put(d, c);
        }

        YearMonth ym = YearMonth.of(y, m);
        int daysInMonth = ym.lengthOfMonth();
        List<OrderCountByDayDto> result = new ArrayList<>();
        for (int d = 1; d <= daysInMonth; d++) {
            long c = counts.getOrDefault(d, 0L);
            result.add(new OrderCountByDayDto(d, c));
        }

        return ResponseEntity.ok(result);
    }

}
