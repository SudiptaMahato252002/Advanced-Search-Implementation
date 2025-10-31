package com.DatabaseOperations.requests;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.DatabaseOperations.dtos.ProductVariantDto;
import com.DatabaseOperations.entities.ProductAttribute;

import lombok.Data;

@Data
public class ProductRequest 
{
     private String sku;
    private String name;
    private String slug;

    private Boolean isActive;

    private String shortDescription;
    private String fullDescription;

    private BigDecimal basePrice;
    private BigDecimal discountedPrice;
    private Integer discountedPercentage;

    private String currency;

    private Integer stockQuantity;

    private Long categoryId;
    private Long brandId;

    private Set<String> tags;

    private List<ProductVariantDto> variants;
    private List<ProductAttribute> attributes;
    
}
