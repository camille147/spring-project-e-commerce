package org.example.shared.entityForm;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdressForm {
    @NotBlank(message = "La rue est obligatoire")
    private String street;

    @NotBlank(message = "La ville est obligatoire")
    private String city;

    @NotBlank(message = "Le code postal est obligatoire")
    private String zipCode;

    @NotBlank(message = "Le pays est obligatoire")
    private String country;

    private boolean isActive = true;
}
