package org.example.springecommerceapi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPromotionDto {
    private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long productId;
    private Long promotionId;
}
