package org.example.springecommerce.controller;

import org.example.shared.entityForm.ProductForm;
import org.example.shared.model.entity.Category;
import org.example.shared.model.entity.Order;
import org.example.shared.model.entity.Product;
import org.example.shared.model.enumeration.OrderStatus;
import org.example.shared.repository.*;
import org.example.springecommerce.controller.dto.BestSellerDto;
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

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Controller
public class AdminController {

    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private OrderLineRepository orderLineRepository;

    // --- DASHBOARD ---
    @GetMapping("/admin/dashboard")
    public String adminHome(Model model) {
        // 1. Définir la page active pour la sidebar (ESSENTIEL)
        model.addAttribute("activePage", "dashboard");

        // 2. Statistiques simples
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalOrders", orderRepository.count());
        Double totalRevenue = orderRepository.sumTotalAmount();
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);

        // 3. Best Sellers (Logique sécurisée)
        List<Object[]> bestSellersRaw = orderLineRepository.findSalesCountGroupedByProduct();
        List<BestSellerDto> bestSellers = new ArrayList<>();

        int limit = 5;
        int count = 0;

        for (Object[] row : bestSellersRaw) {
            if (row != null && row.length >= 2) {
                // Récupération sécurisée des IDs et Counts
                Long productId = ((Number) row[0]).longValue();
                Long salesCount = ((Number) row[1]).longValue();

                // On charge le produit via l'ID pour éviter les erreurs de cast
                Optional<Product> productOpt = productRepository.findById(productId);

                if (productOpt.isPresent()) {
                    Product p = productOpt.get();
                    BestSellerDto dto = new BestSellerDto(
                            p.getId(),
                            p.getProductName(),
                            p.getReference(),
                            p.getPrice(),
                            p.getQuantity(),
                            salesCount
                    );
                    bestSellers.add(dto);
                }

                count++;
                if (count >= limit) break;
            }
        }

        model.addAttribute("bestSellers", bestSellers);

        // 4. Dates pour le graphique (Optionnel, utile si vous l'utilisez dans la vue)
        LocalDate now = LocalDate.now();
        model.addAttribute("currentMonth", now.getMonthValue());
        model.addAttribute("currentYear", now.getYear());

        return "admin/dashboard";
    }

    // --- STATISTIQUES JSON (Graphique) ---
    @GetMapping("/admin/dashboard/stats")
    @ResponseBody
    public ResponseEntity<List<OrderCountByDayDto>> ordersStats(
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year) {

        LocalDate now = LocalDate.now();
        int m = (month != null) ? month : now.getMonthValue();
        int y = (year != null) ? year : now.getYear();

        List<Object[]> raw = orderRepository.findOrderCountByMonthYear(m, y);
        Map<Integer, Long> counts = new HashMap<>();

        for (Object[] row : raw) {
            if (row != null && row.length >= 2) {
                int d = ((Number) row[0]).intValue();
                long c = ((Number) row[1]).longValue();
                counts.put(d, c);
            }
        }

        YearMonth ym = YearMonth.of(y, m);
        int daysInMonth = ym.lengthOfMonth();
        List<OrderCountByDayDto> result = new ArrayList<>();

        for (int d = 1; d <= daysInMonth; d++) {
            result.add(new OrderCountByDayDto(d, counts.getOrDefault(d, 0L)));
        }

        return ResponseEntity.ok(result);
    }

    // --- UTILISATEURS ---
    @GetMapping("/admin/users")
    public String adminUsers(Model model) {
        model.addAttribute("activePage", "users");
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    // --- REMISES ---
    @GetMapping("/admin/discounts")
    public String adminDiscounts(Model model) {
        model.addAttribute("activePage", "discounts");
        return "admin/discounts";
    }

    // --- PRODUITS ---
    @GetMapping("/admin/products")
    public String adminProducts(Model model,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "10") int size) {

        model.addAttribute("activePage", "products");

        Page<Product> pageProducts = productRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
        model.addAttribute("products", pageProducts.getContent());
        model.addAttribute("currentPage", pageProducts.getNumber());
        model.addAttribute("totalPages", pageProducts.getTotalPages());
        model.addAttribute("pageSize", pageProducts.getSize());
        model.addAttribute("totalItems", pageProducts.getTotalElements());
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/products";
    }

    @GetMapping("/admin/products/search")
    public String searchProduct(Model model, @RequestParam(value = "search", required = false) String search) {
        model.addAttribute("activePage", "products");
        model.addAttribute("search", search);
        return "admin/products";
    }

    @GetMapping("/admin/products/edit")
    public String editProductForm(@RequestParam(value = "id", required = false) Long id, Model model) {
        model.addAttribute("activePage", "products");

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
            model.addAttribute("activePage", "products");
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
    public String deleteProduct(@PathVariable("id") Long id,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "10") int size) {
        productRepository.deleteById(id);
        return String.format("redirect:/admin/products?page=%d&size=%d", page, size);
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


    // --- COMMANDES (Orders) ---
    @GetMapping("/admin/orders")
    public String adminOrders(Model model,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size,
                              @RequestParam(value = "client", required = false) String client,
                              @RequestParam(value = "status", required = false) Integer status,
                              @RequestParam(value = "number", required = false) String number) {

        model.addAttribute("activePage", "orders");

        Page<Order> ordersPage;
        if ((client != null && !client.isBlank()) || status != null || (number != null && !number.isBlank())) {
            ordersPage = orderRepository.searchOrders(
                    (client != null && !client.isBlank()) ? client : null,
                    status,
                    (number != null && !number.isBlank()) ? number : null,
                    PageRequest.of(page, size, Sort.by("id").descending())
            );
        } else {
            ordersPage = orderRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()));
        }

        model.addAttribute("orders", ordersPage.getContent());
        model.addAttribute("currentPage", ordersPage.getNumber());
        model.addAttribute("totalPages", ordersPage.getTotalPages());
        model.addAttribute("pageSize", ordersPage.getSize());
        model.addAttribute("totalItems", ordersPage.getTotalElements());

        model.addAttribute("client", client);
        model.addAttribute("statusFilter", status);
        model.addAttribute("number", number);

        return "admin/orders";
    }

    @GetMapping("/admin/orders/{id}/data")
    public ResponseEntity<Map<String, Object>> getOrderData(@PathVariable("id") Long id) {
        Optional<Order> maybe = orderRepository.findById(id);
        if (maybe.isEmpty()) return ResponseEntity.notFound().build();

        Order o = maybe.get();
        Map<String, Object> data = new HashMap<>();
        data.put("id", o.getId());
        data.put("orderNumber", o.getOrderNumber());
        data.put("customerName", o.getUser() != null ? (o.getUser().getFirstName() + " " + o.getUser().getLastName()) : "Inconnu");
        data.put("totalAmount", o.getTotal());
        data.put("status", o.getStatus());
        data.put("statusEnumName", OrderStatus.fromCode(o.getStatus()).name());

        return ResponseEntity.ok(data);
    }

    @PostMapping("/admin/orders/update")
    public String updateOrder(@RequestParam("id") Long id, @RequestParam("status") String status,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size) {
        Optional<Order> maybe = orderRepository.findById(id);
        if (maybe.isPresent()) {
            Order o = maybe.get();
            int statusCode;
            try {
                statusCode = OrderStatus.valueOf(status).getCode();
            } catch (IllegalArgumentException ex) {
                try {
                    statusCode = Integer.parseInt(status);
                } catch (NumberFormatException nfe) {
                    statusCode = OrderStatus.PENDING.getCode();
                }
            }
            o.setStatus(statusCode);
            orderRepository.save(o);
        }
        return String.format("redirect:/admin/orders?page=%d&size=%d", page, size);
    }

    @PostMapping("/admin/orders/delete/{id}")
    public String deleteOrder(@PathVariable("id") Long id,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size) {
        orderRepository.deleteById(id);
        return String.format("redirect:/admin/orders?page=%d&size=%d", page, size);
    }
}