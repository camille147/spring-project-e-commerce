package org.example.shared.entityForm;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserRegisterForm {
    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est requis")
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 8, message = "8 caractères minimum")
    private String password;

    @NotBlank(message = "Veuillez confirmer le mot de passe")
    private String confirmPassword;

    @NotBlank(message = "Prénom requis")
    private String firstName;

    @NotBlank(message = "Nom requis")
    private String lastName;

    @NotNull(message = "Date de naissance requise")
    private LocalDate birthDate;

    @NotNull(message = "Vous devez accepter la politique de confidentialité")
    private Boolean privacyConsent;
}
