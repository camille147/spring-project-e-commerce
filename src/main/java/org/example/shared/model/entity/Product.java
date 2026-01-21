package org.example.shared.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Table(name = "product")
@Data

public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @NotBlank(message = "The product name is mandatory")
    @Size(min = 3)
    private String productName;

    @Column(nullable = false)
    @NotBlank(message = "The product description is mandatory")
    @Size(min = 3)
    private String description;

    @Column(nullable = false)
    @NotBlank(message = "The product price is mandatory")
    private Double price;

    @Column(nullable = false)
    @NotBlank(message = "The product quantity is mandatory")
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    @NotBlank(message = "The product status is mandatory")
    private Boolean isEnabled = true;

    @Column(nullable = false)
    @NotBlank(message = "The color product is mandatory")
    private String color;

    @Column(nullable = false)
    @NotBlank(message = "The brand product is mandatory")
    private String brand;

    @Column(nullable = false)
    @NotBlank(message = "The reference product is mandatory")
    private String reference;
}
