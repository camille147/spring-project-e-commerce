package org.example.springecommerce.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.example.shared.model.DTO.CartItem;
import org.example.shared.model.entity.*;
import org.example.shared.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user/cart")
public class CartController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderLineRepository orderLineRepository;

    @Autowired private AddressRepository addressRepository;

    @Autowired
    private OrderRepository orderRepository;


    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam("quantity") int quantity,
                            HttpSession session) {

        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        if (cart.containsKey(productId)) {
            CartItem item = cart.get(productId);
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            cart.put(productId, new CartItem(product, quantity));
        }

        return "redirect:/user/shop";
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        if (cart == null) cart = new HashMap<>();

        double total = cart.values().stream()
                .mapToDouble(CartItem::getSubTotal)
                .sum();

        model.addAttribute("cartItems", cart.values());
        model.addAttribute("total", total);
        return "user/cart";
    }


    @PostMapping("/remove")
    public String removeFromCart(HttpSession session, @RequestParam Long itemId) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");

        if (cart != null) {
            cart.remove(itemId);

            if (cart.isEmpty()) {
                session.removeAttribute("cart");
            } else {
                session.setAttribute("cart", cart);
            }
        }
        return "redirect:/user/cart";
    }

    @PostMapping("/update-quantity")
    public String updateQuantity(@RequestParam Long itemId,
                                 @RequestParam String action,
                                 HttpSession session) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        if (cart != null && cart.containsKey(itemId)) {
            CartItem item = cart.get(itemId);
            if ("add".equals(action)) {
                item.setQuantity(item.getQuantity() + 1);
            } else if ("remove".equals(action) && item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
            }
            session.setAttribute("cart", cart);
        }
        return "redirect:/user/cart";
    }


    @PostMapping("/checkout/validate")
    @Transactional // tout doit réussir ou tout échouer
    public String processCheckout(HttpSession session, Principal principal) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) return "redirect:/user/cart";

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(0);

        Address address = addressRepository.findByUserAndIsActiveTrue(user)
                .stream().findFirst().orElseThrow(() -> new RuntimeException("Adresse manquante"));
        order.setAddress(address);

        double total = cart.values().stream().mapToDouble(CartItem::getSubTotal).sum();
        order.setTotal(total);

        Order savedOrder = orderRepository.save(order);

        for (CartItem item : cart.values()) {
            OrderLine line = new OrderLine();
            line.setOrder(savedOrder);
            line.setProduct(item.getProduct());
            line.setQuantity(item.getQuantity());
            line.setPrice(item.getProduct().getEffectivePrice());
            orderLineRepository.save(line);

            Product p = item.getProduct();
            p.setQuantity(p.getQuantity() - item.getQuantity());
            productRepository.save(p);
        }

        session.removeAttribute("cart");

        return "redirect:/user/profile?success=order_confirmed";
    }

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model, @AuthenticationPrincipal UserDetails principal) {

        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");

        double total = cart.values().stream()
                .mapToDouble(CartItem::getSubTotal)
                .sum();
        model.addAttribute("user", user);
        model.addAttribute("cartItems", cart.values());
        model.addAttribute("total", total);

        return "user/checkout";
    }


    private String generateOrderNumber() {
        return String.valueOf((int)(Math.random() * 900000000 + 100000000)); // Génère 9 chiffres
    }

}
