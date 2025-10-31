package com.DatabaseOperations.requests;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductVariantCreateRequest 
{
     private Long productId;

    private String sku;
    private String variantName;

    private String color;
    private String size;
    private String storage;
    private String material;

    private BigDecimal additionalPrice;
    private Integer stockQuantity;

    private Boolean isDefault;
    private Integer displayOrder;
    
}
