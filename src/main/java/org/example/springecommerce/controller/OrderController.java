package org.example.springecommerce.controller;

import org.example.shared.model.entity.Order;
import org.example.shared.model.service.OrderService;
import org.example.shared.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional; // Import Spring préférable
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @PostMapping("/user/orders/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, Principal principal) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        if (!order.getUser().getEmail().equals(principal.getName())) {
            return "redirect:/user/profile?error=unauthorized";
        }

        if (order.getStatus() == 0 || order.getStatus() == 1) {
            orderService.cancelOrder(order);
            return "redirect:/user/order/" + id + "?success=cancelled";
        }

        return "redirect:/user/profile?error=cannot-cancel";
    }

    @GetMapping("/user/order/{id}")
    @Transactional
    public String getOrderDetail(@PathVariable("id") Long id, Model model, Principal principal) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        if (!order.getUser().getEmail().equals(principal.getName())) {
            return "redirect:/user/profile?error=unauthorized";
        }

        model.addAttribute("order", order);

        return "user/order-detail";
    }

    @GetMapping("/user/order/confirmation/{id}")
    @Transactional // Indispensable pour charger les produits (orderLines) pour le résumé
    public String showConfirmationPage(@PathVariable Long id, Model model, Principal principal) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        if (!order.getUser().getEmail().equals(principal.getName())) {
            return "redirect:/user/shop";
        }

        model.addAttribute("order", order);
        return "user/order-confirmation";
    }
}