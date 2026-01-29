package org.example.springecommerceapi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private List<Long> categoryIds;
    private Boolean isEnabled;
    private String color;
    private String brand;
    private String reference;
}
