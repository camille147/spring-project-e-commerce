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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
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

    @GetMapping("/admin/orders")
    public List<OrderDto> findAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderDto> findById(@PathVariable Long id) {
        Optional<Order> o = repository.findById(id);
        if (o.isEmpty()) return ResponseEntity.notFound().build();
        Order order = o.get();

        if (isNotOwnerNorAdmin(order.getUser())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(toDto(order));
    }

    @PostMapping("/orders")
    public ResponseEntity<?> create(@Valid @RequestBody OrderDto dto) {
        User current = getAuthenticatedUser();
        if (current == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        User targetUser = current;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(g -> "ROLE_ADMIN".equals(g.getAuthority()));
        if (isAdmin && dto.getUserId() != null) {
            Optional<User> maybeUser = userRepository.findById(dto.getUserId());
            if (maybeUser.isEmpty()) return ResponseEntity.badRequest().body("Invalid userId");
            targetUser = maybeUser.get();
        }

        if (dto.getAddressId() != null) {
            Optional<Address> maybeAddress = addressRepository.findById(dto.getAddressId());
            if (maybeAddress.isEmpty()) return ResponseEntity.badRequest().body("Invalid addressId");
            Address addr = maybeAddress.get();
            if (!isAdmin && (addr.getUser() == null || !addr.getUser().getId().equals(targetUser.getId()))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Address does not belong to the user");
            }
        }

        Order entity = toEntity(dto);
        entity.setUser(targetUser);
        Order saved = repository.save(entity);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(toDto(saved));
    }

    @PatchMapping("/admin/orders/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        Optional<Order> maybeOrder = repository.findById(id);
        if (maybeOrder.isEmpty()) return ResponseEntity.notFound().build();
        Order order = maybeOrder.get();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(g -> "ROLE_ADMIN".equals(g.getAuthority()));
        if (!isAdmin) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        if (!payload.containsKey("status")) return ResponseEntity.badRequest().body("Payload must contain 'status' integer");
        Integer status = payload.get("status");
        if (status == null) return ResponseEntity.badRequest().body("Invalid status");

        order.setStatus(status);
        Order saved = repository.save(order);
        return ResponseEntity.ok(toDto(saved));
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

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        String email = auth.getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    private boolean isOwnerOrAdmin(User owner) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return false;
        String email = auth.getName();
        if (owner == null) return false;
        return email.equals(owner.getEmail());
    }

    private boolean isNotOwnerNorAdmin(User owner) {
        return !isOwnerOrAdmin(owner);
    }
}
