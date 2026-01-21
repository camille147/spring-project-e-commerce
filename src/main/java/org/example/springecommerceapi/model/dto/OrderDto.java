package org.example.springecommerceapi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private Long id;
    private String orderNumber;
    private LocalDateTime createdAt;
    private Double total;
    private Integer status;
    private Long userId;
    private Long addressId;
    private List<Long> orderLineIds;
}
