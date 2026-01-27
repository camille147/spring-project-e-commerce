package org.example.springecommerceapi.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;

    @NotBlank(message = "Le prénom est requis")
    @Size(min = 2, message = "Le prénom doit contenir au moins 2 caractères")
    private String firstName;

    @NotBlank(message = "Le nom est requis")
    @Size(min = 2, message = "Le nom doit contenir au moins 2 caractères")
    private String lastName;

    @NotBlank(message = "L'email est requis")
    @Email(message = "Email invalide")
    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @AssertTrue(message = "L'utilisateur doit avoir entre 18 et 130 ans")
    public boolean isAgeValid() {
        if (this.birthDate == null) return false;
        int years = Period.between(this.birthDate, LocalDate.now()).getYears();
        return years >= 18 && years <= 130;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    @Column(name = "is_activated")
    private Boolean isActivated = true;
}
