package org.example.springecommerce.controller;

import org.example.shared.entityForm.ProductForm;
import org.example.shared.model.entity.*;
import org.example.shared.model.enumeration.OrderStatus;
import org.example.shared.model.enumeration.UserRole;
import org.example.shared.repository.*;
import org.example.springecommerce.controller.dto.BestSellerDto;
import org.example.springecommerce.controller.dto.OrderCountByDayDto;
// Assurez-vous que le package correspond à l'endroit où vous avez créé votre fichier
import org.example.springecommerce.controller.dto.PromotionViewDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private OrderLineRepository orderLineRepository;
    @Autowired private PromotionRepository promotionRepository;
    @Autowired private ProductPromotionRepository productPromotionRepository;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    // ==========================================
    //               DASHBOARD
    // ==========================================
    @GetMapping("/admin/dashboard")
    public String adminHome(Model model) {
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalOrders", orderRepository.count());
        Double totalRevenue = orderRepository.sumTotalAmount();
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);

        List<Object[]> bestSellersRaw = orderLineRepository.findSalesCountGroupedByProduct();
        List<BestSellerDto> bestSellers = new ArrayList<>();
        int limit = 5;
        int count = 0;
        for (Object[] row : bestSellersRaw) {
            if (row != null && row.length >= 2) {
                Long productId = ((Number) row[0]).longValue();
                Long salesCount = ((Number) row[1]).longValue();
                Optional<Product> productOpt = productRepository.findById(productId);
                if (productOpt.isPresent()) {
                    Product p = productOpt.get();
                    bestSellers.add(new BestSellerDto(p.getId(), p.getProductName(), p.getReference(), p.getPrice(), p.getQuantity(), salesCount));
                }
                count++;
                if (count >= limit) break;
            }
        }
        model.addAttribute("bestSellers", bestSellers);
        return "admin/dashboard";
    }

    @GetMapping("/admin/dashboard/stats")
    @ResponseBody
    public ResponseEntity<List<OrderCountByDayDto>> ordersStats(@RequestParam(value = "month", required = false) Integer month,
                                                                @RequestParam(value = "year", required = false) Integer year) {
        LocalDate now = LocalDate.now();
        int m = (month != null) ? month : now.getMonthValue();
        int y = (year != null) ? year : now.getYear();
        List<Object[]> raw = orderRepository.findOrderCountByMonthYear(m, y);
        Map<Integer, Long> counts = new HashMap<>();
        for (Object[] row : raw) {
            if (row != null && row.length >= 2) {
                counts.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
            }
        }
        YearMonth ym = YearMonth.of(y, m);
        List<OrderCountByDayDto> result = new ArrayList<>();
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            result.add(new OrderCountByDayDto(d, counts.getOrDefault(d, 0L)));
        }
        return ResponseEntity.ok(result);
    }

    // ==========================================
    //               UTILISATEURS
    // ==========================================

    @GetMapping("/admin/users")
    public String adminUsers(Model model,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "10") int size,
                             @RequestParam(value = "search", required = false) String search,
                             @RequestParam(value = "role", required = false) String roleStr) {
        model.addAttribute("activePage", "users");
        UserRole role = null;
        if (roleStr != null && !roleStr.isBlank()) { try { role = UserRole.valueOf(roleStr); } catch (Exception e) {} }
        Page<User> userPage = userRepository.searchAndFilter(search, role, PageRequest.of(page, size, Sort.by("id").descending()));
        model.addAttribute("users", userPage.getContent()); model.addAttribute("currentPage", userPage.getNumber()); model.addAttribute("totalPages", userPage.getTotalPages()); model.addAttribute("pageSize", userPage.getSize()); model.addAttribute("totalItems", userPage.getTotalElements()); model.addAttribute("search", search); model.addAttribute("role", roleStr);
        return "admin/users";
    }

    @PostMapping("/admin/users/save")
    public String saveUser(@RequestParam(value = "id", required = false) Long id,
                           @RequestParam("firstName") String firstName,
                           @RequestParam("lastName") String lastName,
                           @RequestParam(value = "email", required = false) String email,         // Optionnel en update
                           @RequestParam(value = "birthDate", required = false) LocalDate birthDate, // Optionnel en update
                           @RequestParam(value = "password", required = false) String password,   // Optionnel en update
                           @RequestParam(value = "active", required = false) String activeCheckbox,
                           RedirectAttributes redirectAttributes) {

        User user;
        boolean isNew = (id == null);

        if (isNew) {

            if (birthDate == null || Period.between(birthDate, LocalDate.now()).getYears() < 18) {
                redirectAttributes.addFlashAttribute("error", "Impossible de créer un administrateur mineur. L'âge minimum est de 18 ans.");
                return "redirect:/admin/users";
            }

            user = new User();
            user.setRole(UserRole.ADMIN);

            user.setEmail(email);
            user.setBirthDate(birthDate);

            String passToHash = (password != null && !password.isBlank()) ? password : "Password123!";
            user.setPassword(passwordEncoder != null ? passwordEncoder.encode(passToHash) : passToHash);

        } else {
            Optional<User> existing = userRepository.findById(id);
            if (existing.isEmpty()) return "redirect:/admin/users";

            user = existing.get();
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setIsActivated(activeCheckbox != null);

        userRepository.save(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) { Optional<User> userOpt = userRepository.findById(id); if (userOpt.isPresent()) { User u = userOpt.get(); u.setIsActivated(false); userRepository.save(u); } return "redirect:/admin/users"; }

    @GetMapping("/admin/users/{id}/data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserData(@PathVariable("id") Long id) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isPresent()) {
            User u = opt.get();
            Map<String, Object> data = new HashMap<>();
            data.put("id", u.getId());
            data.put("firstName", u.getFirstName());
            data.put("lastName", u.getLastName());
            data.put("email", u.getEmail());
            data.put("birthDate", u.getBirthDate()); // AJOUTÉ
            data.put("role", u.getRole().name());
            data.put("active", u.getIsActivated());
            return ResponseEntity.ok(data);
        }
        return ResponseEntity.notFound().build();
    }



    // ==========================================
    //               REMISES (Promotions)
    // ==========================================

    @GetMapping("/admin/discounts")
    public String adminDiscounts(Model model,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "10") int size,
                                 @RequestParam(value = "search", required = false) String search) {
        model.addAttribute("activePage", "discounts");

        Page<Promotion> promoPage = promotionRepository.searchByName(search, PageRequest.of(page, size, Sort.by("id").descending()));
        LocalDateTime now = LocalDateTime.now();

        // Transformation en DTO pour l'affichage
        List<PromotionViewDto> dtos = promoPage.getContent().stream().map(p -> {
            PromotionViewDto dto = new PromotionViewDto();
            dto.setId(p.getId());
            dto.setName(p.getName());
            dto.setDiscountRate(p.getDiscountRate());

            List<ProductPromotion> pps = productPromotionRepository.findAll().stream()
                    .filter(pp -> pp.getPromotion().getId().equals(p.getId()))
                    .collect(Collectors.toList());

            if (!pps.isEmpty()) {
                LocalDateTime minStart = pps.stream().map(ProductPromotion::getStartDate).min(Comparator.naturalOrder()).orElse(null);
                LocalDateTime maxEnd = pps.stream().map(ProductPromotion::getEndDate).max(Comparator.naturalOrder()).orElse(null);
                dto.setStartDate(minStart);
                dto.setEndDate(maxEnd);

                if (minStart != null && maxEnd != null) {
                    if (now.isBefore(minStart)) { dto.setStatus("Prochainement"); dto.setStatusColor("bg-blue-100 text-blue-700"); }
                    else if (now.isAfter(maxEnd)) { dto.setStatus("Fini"); dto.setStatusColor("bg-gray-100 text-gray-500"); }
                    else { dto.setStatus("Actif"); dto.setStatusColor("bg-green-100 text-green-700"); }
                }
            } else {
                dto.setStatus("Inactif"); dto.setStatusColor("bg-gray-100 text-gray-400");
            }
            return dto;
        }).collect(Collectors.toList());

        model.addAttribute("promotions", dtos);
        model.addAttribute("currentPage", promoPage.getNumber());
        model.addAttribute("totalPages", promoPage.getTotalPages());
        model.addAttribute("pageSize", promoPage.getSize());
        model.addAttribute("totalItems", promoPage.getTotalElements());
        model.addAttribute("search", search);
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());

        // --- AJOUT IMPORTANT : La liste des produits pour le Select Multiple ---
        model.addAttribute("products", productRepository.findAll());

        return "admin/discounts";
    }

    @PostMapping("/admin/discounts/save")
    public String saveDiscount(@RequestParam(value = "id", required = false) Long id,
                               @RequestParam("name") String name,
                               @RequestParam("discountRate") Double discountRate,
                               @RequestParam(value = "startDate", required = false) String startDateStr,
                               @RequestParam(value = "endDate", required = false) String endDateStr,
                               @RequestParam(value = "productIds", required = false) List<Long> productIds) {

        // 1. Sauvegarde de la Promotion de base
        Promotion p;
        if (id != null) {
            p = promotionRepository.findById(id).orElse(new Promotion());
        } else {
            p = new Promotion();
        }
        p.setName(name);
        p.setDiscountRate(discountRate);
        p = promotionRepository.save(p);

        // 2. Gestion des produits et des dates
        // Si des dates et des produits sont fournis
        if (startDateStr != null && !startDateStr.isBlank() && endDateStr != null && !endDateStr.isBlank()) {

            // Conversion des dates (String -> LocalDateTime)
            LocalDateTime start = LocalDate.parse(startDateStr).atStartOfDay();
            LocalDateTime end = LocalDate.parse(endDateStr).atTime(23, 59, 59);

            // Nettoyage des anciennes liaisons pour cette promotion (Méthode simple sans changer le repo)
            final Long promoId = p.getId();
            List<ProductPromotion> oldLinks = productPromotionRepository.findAll().stream()
                    .filter(pp -> pp.getPromotion().getId().equals(promoId))
                    .collect(Collectors.toList());
            productPromotionRepository.deleteAll(oldLinks);

            // Création des nouvelles liaisons
            if (productIds != null && !productIds.isEmpty()) {
                for (Long prodId : productIds) {
                    Optional<Product> prodOpt = productRepository.findById(prodId);
                    if (prodOpt.isPresent()) {
                        ProductPromotion pp = new ProductPromotion();
                        pp.setPromotion(p);
                        pp.setProduct(prodOpt.get());
                        pp.setStartDate(start);
                        pp.setEndDate(end);
                        productPromotionRepository.save(pp);
                    }
                }
            }
        }

        return "redirect:/admin/discounts";
    }

    @GetMapping("/admin/discounts/{id}/data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDiscountData(@PathVariable("id") Long id) {
        Optional<Promotion> promoOpt = promotionRepository.findById(id);
        if (promoOpt.isEmpty()) return ResponseEntity.notFound().build();

        Promotion p = promoOpt.get();
        Map<String, Object> data = new HashMap<>();
        data.put("id", p.getId());
        data.put("name", p.getName());
        data.put("discountRate", p.getDiscountRate());

        List<ProductPromotion> links = productPromotionRepository.findAll().stream()
                .filter(pp -> pp.getPromotion().getId().equals(p.getId()))
                .collect(Collectors.toList());

        // Liste des IDs produits
        List<Long> productIds = links.stream().map(pp -> pp.getProduct().getId()).collect(Collectors.toList());
        data.put("productIds", productIds);

        // Dates (on prend la première trouvée, supposant qu'elles sont groupées)
        if (!links.isEmpty()) {
            data.put("startDate", links.get(0).getStartDate().toLocalDate().toString());
            data.put("endDate", links.get(0).getEndDate().toLocalDate().toString());
        }

        return ResponseEntity.ok(data);
    }


    // ==========================================
    //               PRODUITS & COMMANDES
    // ==========================================

    @GetMapping("/admin/products")
    public String adminProducts(Model model,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "10") int size,
                                @RequestParam(value = "search", required = false) String search,
                                @RequestParam(value = "categoryId", required = false) Long categoryId) {
        model.addAttribute("activePage", "products");
        Page<Product> pageProducts = productRepository.searchAndFilter(search, categoryId, PageRequest.of(page, size, Sort.by("id").descending()));
        model.addAttribute("products", pageProducts.getContent());
        model.addAttribute("currentPage", pageProducts.getNumber());
        model.addAttribute("totalPages", pageProducts.getTotalPages());
        model.addAttribute("pageSize", pageProducts.getSize());
        model.addAttribute("totalItems", pageProducts.getTotalElements());
        model.addAttribute("search", search);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categories", categoryRepository.findAll());
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
        Product p = (form.getId() != null) ? productRepository.findById(form.getId()).orElse(new Product()) : new Product();
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
            ordersPage = orderRepository.searchOrders(client, status, number, PageRequest.of(page, size, Sort.by("id").descending()));
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
            try { statusCode = OrderStatus.valueOf(status).getCode(); }
            catch (Exception ex) { statusCode = OrderStatus.PENDING.getCode(); }
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

    @GetMapping("/admin/profile")
    public String adminProfile(Model model, Principal principal) {
        model.addAttribute("activePage", "profile"); // Pour le menu (optionnel si vous l'ajoutez)

        if (principal != null) {
            String email = principal.getName();
            Optional<User> adminOpt = userRepository.findByEmail(email);
            if (adminOpt.isPresent()) {
                model.addAttribute("admin", adminOpt.get());
                return "admin/profile";
            }
        }
        return "redirect:/login"; // Sécurité si non connecté
    }

    @PostMapping("/admin/profile/update")
    public String updateAdminProfile(@RequestParam("firstName") String firstName,
                                     @RequestParam("lastName") String lastName,
                                     @RequestParam("email") String email,
                                     @RequestParam(value = "currentPassword", required = false) String currentPassword,
                                     @RequestParam(value = "newPassword", required = false) String newPassword,
                                     @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                                     Principal principal,
                                     Model model) {

        if (principal == null) return "redirect:/login";

        String currentEmail = principal.getName();
        Optional<User> adminOpt = userRepository.findByEmail(currentEmail);

        if (adminOpt.isPresent()) {
            User admin = adminOpt.get();

            admin.setFirstName(firstName);
            admin.setLastName(lastName);

            if (newPassword != null && !newPassword.isBlank()) {

                if (newPassword.equals(confirmPassword)) {
                    admin.setPassword(passwordEncoder.encode(newPassword));
                } else {
                    model.addAttribute("error", "Les nouveaux mots de passe ne correspondent pas.");
                    model.addAttribute("admin", admin);
                    return "admin/profile";
                }
            }

            userRepository.save(admin);
        }

        return "redirect:/admin/profile?success";
    }
}
