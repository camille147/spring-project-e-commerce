package org.example.springecommerceapi.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.Period;

@Data
public class SignupRequest{
    @Email
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Email invalide")
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @AssertTrue(message = "L'utilisateur doit avoir entre 18 et 130 ans")
    public boolean isAgeValid() {
        if (this.birthDate == null) return false;
        int years = Period.between(this.birthDate, LocalDate.now()).getYears();
        return years >= 18 && years <= 130;
    }

}
