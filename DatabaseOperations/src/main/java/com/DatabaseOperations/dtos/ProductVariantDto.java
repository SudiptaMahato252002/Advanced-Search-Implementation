package com.DatabaseOperations.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantDto 
{
     private Long id;

    private Long productId;        // reference by id instead of entity
    private String sku;
    private String variantName;

    private String color;
    private String size;
    private String storage;
    private String material;

    private BigDecimal additionalPrice;
    private Integer stockQuantity;

    private Boolean isAvailable;
    private Boolean isDefault;
    private Integer displayOrder;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
}
