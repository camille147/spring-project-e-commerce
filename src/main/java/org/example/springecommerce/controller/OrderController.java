package org.example.springecommerce.controller;

import org.example.shared.model.entity.Order;
import org.example.shared.model.service.OrderService;
import org.example.shared.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

        // order est à utili connecté
        if (!order.getUser().getEmail().equals(principal.getName())) {
            return "redirect:/user/profile?error=unauthorized";
        }

        // annulation qu esi status en attente ou payée
        if (order.getStatus() == 0 || order.getStatus() == 1) {
            orderService.cancelOrder(order);
            return "redirect:/user/profile?success=order_cancelled";
        }

        return "redirect:/user/profile?error=cannot-cancel";
    }
}
