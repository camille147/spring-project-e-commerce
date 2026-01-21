package org.example.springecommerceapi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLineDto {
    private Long id;
    private Integer quantity;
    private Double price;
    private Long productId;
    private Long orderId;
}
