package org.example.shared.entityForm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressForm {
    @NotBlank(message = "La rue est obligatoire")
    private String street;

    @NotBlank(message = "La ville est obligatoire")
    private String city;

    @NotBlank(message = "Le code postal est obligatoire")
    @Size(min = 5, message = "Le code postal est trop court")
    private String zipCode;

    @NotBlank(message = "Le pays est obligatoire")
    private String country;

    private boolean isActive = true;
}
