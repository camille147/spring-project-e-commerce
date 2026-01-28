package org.example.springecommerce.controller.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PromotionViewDto {
    private Long id;
    private String name;
    private Double discountRate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private String statusColor;
}