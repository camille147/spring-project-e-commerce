package org.example.shared.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
@Table(name = "picture")

public class Picture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "The picture name is mandatory")
    @Size(min = 3)
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "The picture url is mandatory")
    @Size(min = 10)
    private String pictureUrl;

    @Column(nullable = false)
    @NotBlank(message = "The picture type is mandatory")
    private Boolean isActive;
}
