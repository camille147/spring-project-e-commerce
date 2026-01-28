package org.example.shared.entityForm;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProductForm {
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    private String productName;

    @NotBlank(message = "La marque est obligatoire")
    private String brand;

    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit être positif")
    private Double price;

    @NotNull(message = "Le stock est obligatoire")
    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    private Integer quantity;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotBlank(message = "La référence est obligatoire")
    private String reference;

    @NotNull(message = "Veuillez choisir une catégorie")
    private Long categoryId;
}
