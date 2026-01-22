package org.example.shared.model.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Table(name = "address")
@Data
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "The address must have a street")
    @Size(min = 3)
    private String street;

    @Column(nullable = false)
    @NotBlank(message = "The address must have a city")
    @Size(min = 3)
    private String city;

    @Column(nullable = false)
    @NotBlank(message = "The address must have a zip code")
    @Size(min = 3)
    private String ZipCode;

    @Column(nullable = false)
    @NotBlank(message = "The address must have a country")
    @Size(min = 3)
    private String country;

    @Column(nullable = false)
    @NotBlank(message = "The address must have a state")
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


}
