package org.example.springecommerceapi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private Long id;
    private String productName;
    private String description;
    private Double price;
    private Integer quantity;
    private Long categoryId;
    private Boolean isEnabled;
    private String color;
    private String brand;
    private String reference;
}
