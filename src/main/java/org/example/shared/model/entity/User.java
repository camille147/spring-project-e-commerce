package org.example.shared.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.shared.model.enumeration.UserRole;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Table(name = "users")
@Setter
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotBlank(message = "Email is mandatory")
    @Column(nullable = false, unique = true, length = 50)
    private String email;

    //@JsonIgnore
    @NotBlank(message = "Password is mandatory")
    @Size(min = 8)
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Last name is mandatory")
    @Size(min = 3)
    @Column(nullable = false)
    private String lastName;

    @NotBlank(message = "First name is mandatory")
    @Size(min = 3)
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    @Getter
    @Setter
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.of(LocalDate.now(), LocalTime.now());

    @Column
    private Boolean isActivated = true;

    @Column
    private LocalDateTime deletedAt;

    @Column(nullable = false)
    private boolean privacyConsent = false;

    @Column
    private LocalDateTime consentDate;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Order> orders;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Address> addresses;

}
