package org.example.shared.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Le taux de remise est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "La remise doit être supérieure à 0")
    private Double discountRate;

}
