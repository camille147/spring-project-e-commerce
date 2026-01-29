package org.example.shared.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BestSellerDto {
    private Long id;
    private String productName;
    private String reference;
    private Double price;
    private Integer quantity;
    private Long salesCount;
    private String pictureUrl;
}