package com.DatabaseOperations.responses;

import java.math.BigDecimal;
import java.util.List;

import com.DatabaseOperations.dtos.ProductAttributesDto;
import com.DatabaseOperations.dtos.ProductVariantDto;

import lombok.Data;

@Data
public class ProductResponse 
{
     private Long id;
    private String name;
    private String slug;
    private BigDecimal currentPrice;
    private Double avgRating;
    private String stockStatus;
    private Integer orderCount;
    private List<ProductVariantDto> variants;
    private List<ProductAttributesDto> attributes;
    
}
