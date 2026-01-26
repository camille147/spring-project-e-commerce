package org.example.shared.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
@Table(name = "promotion")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "The promotion must have a name")
    @Size(min = 3)
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double discountRate;

}
