package org.example.springecommerceapi.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDto {
    private Long id;

    @NotBlank
    @Size(min = 3)
    private String street;

    @NotBlank
    @Size(min = 3)
    private String city;

    @NotBlank
    @Size(min = 3)
    private String zipCode;

    @NotBlank
    @Size(min = 3)
    private String country;

    private Boolean isActive;
}
