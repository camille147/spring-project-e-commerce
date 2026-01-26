package org.example.shared.model.service;

import org.example.shared.model.entity.Order;
import org.example.shared.model.enumeration.OrderStatus;
import org.example.shared.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
        @Autowired
        private OrderRepository orderRepository;

        public void updateStatus(Long orderId, OrderStatus newStatus) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Commande introuvable"));

            order.setStatus(newStatus.getCode());
            orderRepository.save(order);
        }

}
