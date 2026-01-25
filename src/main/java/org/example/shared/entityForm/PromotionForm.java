package org.example.shared.entityForm;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PromotionForm {
    @NotBlank(message = "Le nom de l'offre est requis")
    private String name;

    @NotNull(message = "Le taux est requis")
    @Min(0) @Max(100)
    private Double discountRate;

    @NotNull(message = "Date de début requise")
    private LocalDateTime startDate;

    @NotNull(message = "Date de fin requise")
    private LocalDateTime endDate;
}
