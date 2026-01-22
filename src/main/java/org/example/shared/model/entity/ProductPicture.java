package org.example.shared.model.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "product_picture")
public class ProductPicture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "picture_id")
    private Picture picture;

}
