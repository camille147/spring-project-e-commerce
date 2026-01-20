package org.example.shared.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;
    private String description;
    private Double price;
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private Boolean isEnabled = true;

}
