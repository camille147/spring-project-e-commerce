package org.example.springecommerceapi.controller;

import org.example.shared.model.entity.OrderLine;
import org.example.shared.model.entity.Order;
import org.example.shared.model.entity.Product;
import org.example.shared.repository.OrderLineRepository;
import org.example.shared.repository.OrderRepository;
import org.example.shared.repository.ProductRepository;
import org.example.springecommerceapi.model.dto.OrderLineDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/order-lines")
public class OrderLineControllerApi {

    private final OrderLineRepository repository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public OrderLineControllerApi(OrderLineRepository repository, ProductRepository productRepository, OrderRepository orderRepository) {
        this.repository = repository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public List<OrderLineDto> findAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderLineDto> findById(@PathVariable Long id) {
        Optional<OrderLine> ol = repository.findById(id);
        return ol.map(o -> ResponseEntity.ok(toDto(o))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderLineDto> create(@Valid @RequestBody OrderLineDto dto) {
        OrderLine entity = toEntity(dto);
        OrderLine saved = repository.save(entity);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderLineDto> update(@PathVariable Long id, @Valid @RequestBody OrderLineDto dto) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        OrderLine entity = toEntity(dto);
        entity.setId(id);
        OrderLine updated = repository.save(entity);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    private OrderLineDto toDto(OrderLine ol) {
        return new OrderLineDto(ol.getId(), ol.getQuantity(), ol.getPrice(), ol.getProduct() != null ? ol.getProduct().getId() : null, ol.getOrder() != null ? ol.getOrder().getId() : null);
    }

    private OrderLine toEntity(OrderLineDto dto) {
        OrderLine ol = new OrderLine();
        ol.setQuantity(dto.getQuantity());
        ol.setPrice(dto.getPrice());
        if (dto.getProductId() != null) {
            Optional<Product> p = productRepository.findById(dto.getProductId());
            p.ifPresent(ol::setProduct);
        }
        if (dto.getOrderId() != null) {
            Optional<Order> o = orderRepository.findById(dto.getOrderId());
            o.ifPresent(ol::setOrder);
        }
        return ol;
    }
}
