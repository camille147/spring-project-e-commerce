package org.example.shared.model.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "The order number is mandatory")
    @Size(min = 9, max = 9)
    private String orderNumber;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @NotBlank(message = "The order total is mandatory")
    private Double total;

    @Column(nullable = false)
    @NotBlank(message = "The order status is mandatory")
    private Integer status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderLine> orderLines;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;

}
