package org.example.shared.model.service;

import jakarta.transaction.Transactional;
import org.example.shared.model.entity.Order;
import org.example.shared.model.entity.OrderLine;
import org.example.shared.model.entity.Product;
import org.example.shared.model.enumeration.OrderStatus;
import org.example.shared.repository.OrderRepository;
import org.example.shared.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
        @Autowired
        private OrderRepository orderRepository;

        @Autowired
        private ProductRepository productRepository;

        public void updateStatus(Long orderId, OrderStatus newStatus) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Commande introuvable"));

            order.setStatus(newStatus.getCode());
            orderRepository.save(order);
        }

    @Transactional
    public void cancelOrder(Order order) {
        // changer le statut CANCELLED
        order.setStatus(4);
        orderRepository.save(order);

        // remettre produits en stock
        for (OrderLine line : order.getOrderLines()) {
            Product p = line.getProduct();
            // rajout de la quantité réservée dans la commande
            p.setQuantity(p.getQuantity() + line.getQuantity());
            productRepository.save(p);
        }
    }


}
