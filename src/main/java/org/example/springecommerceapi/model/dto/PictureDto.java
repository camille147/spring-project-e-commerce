package org.example.springecommerceapi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PictureDto {
    private Long id;
    private String name;
    private String pictureUrl;
    private Boolean isActive;
}
