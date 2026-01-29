package org.example.shared.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private Double price;

    @Column(nullable = false)
    private Integer quantity;

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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "product_category_rel",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "default_picture_id")
    private Picture defaultPicture;

    @OneToMany(mappedBy = "product", fetch = FetchType.EAGER)
    private List<ProductPromotion> productPromotions;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_picture",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "picture_id")
    )
    private List<Picture> gallery;


    public Double getEffectivePrice() {
        LocalDateTime now = LocalDateTime.now();
        // promo active  -> entre start date et end date
        return productPromotions.stream()
                .filter(pp -> pp.getStartDate().isBefore(now) &&
                        (pp.getEndDate() == null || pp.getEndDate().isAfter(now)))
                .map(pp -> this.price * (1 - pp.getPromotion().getDiscountRate() / 100))
                .findFirst()
                .orElse(this.price);
    }

    public boolean isOnSale() {
        LocalDateTime now = LocalDateTime.now();
        return productPromotions.stream()
                .anyMatch(pp -> pp.getStartDate().isBefore(now) &&
                        (pp.getEndDate() == null || pp.getEndDate().isAfter(now)));
    }
}
