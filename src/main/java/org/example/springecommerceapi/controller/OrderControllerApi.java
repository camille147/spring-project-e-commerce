package org.example.springecommerceapi.controller;

import org.example.shared.model.entity.Address;
import org.example.shared.model.entity.Order;
import org.example.shared.model.entity.OrderLine;
import org.example.shared.model.entity.User;
import org.example.shared.repository.AddressRepository;
import org.example.shared.repository.OrderLineRepository;
import org.example.shared.repository.OrderRepository;
import org.example.shared.repository.UserRepository;
import org.example.springecommerceapi.model.dto.OrderDto;
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
@RequestMapping("/api/orders")
public class OrderControllerApi {

    private final OrderRepository repository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final OrderLineRepository orderLineRepository;

    public OrderControllerApi(OrderRepository repository, UserRepository userRepository, AddressRepository addressRepository, OrderLineRepository orderLineRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.orderLineRepository = orderLineRepository;
    }

    @GetMapping
    public List<OrderDto> findAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> findById(@PathVariable Long id) {
        Optional<Order> o = repository.findById(id);
        return o.map(ord -> ResponseEntity.ok(toDto(ord))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderDto> create(@Valid @RequestBody OrderDto dto) {
        Order entity = toEntity(dto);
        Order saved = repository.save(entity);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDto> update(@PathVariable Long id, @Valid @RequestBody OrderDto dto) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Order entity = toEntity(dto);
        entity.setId(id);
        Order updated = repository.save(entity);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    private OrderDto toDto(Order o) {
        List<Long> lineIds = null;
        if (o.getOrderLines() != null) {
            lineIds = o.getOrderLines().stream().map(OrderLine::getId).collect(Collectors.toList());
        }
        return new OrderDto(o.getId(), o.getOrderNumber(), o.getCreatedAt(), o.getTotal(), o.getStatus(), o.getUser() != null ? o.getUser().getId() : null, o.getAddress() != null ? o.getAddress().getId() : null, lineIds);
    }

    private Order toEntity(OrderDto dto) {
        Order o = new Order();
        o.setOrderNumber(dto.getOrderNumber());
        o.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : o.getCreatedAt());
        o.setTotal(dto.getTotal());
        o.setStatus(dto.getStatus());
        if (dto.getUserId() != null) {
            Optional<User> u = userRepository.findById(dto.getUserId());
            u.ifPresent(o::setUser);
        }
        if (dto.getAddressId() != null) {
            Optional<Address> a = addressRepository.findById(dto.getAddressId());
            a.ifPresent(o::setAddress);
        }
        if (dto.getOrderLineIds() != null) {
            List<OrderLine> lines = dto.getOrderLineIds().stream().map(id -> orderLineRepository.findById(id).orElse(null)).filter(l -> l != null).collect(Collectors.toList());
            o.setOrderLines(lines);
        }
        return o;
    }
}
