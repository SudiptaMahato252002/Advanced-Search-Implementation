package com.DatabaseOperations.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.DatabaseOperations.enums.StockStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto 
{
    private Long id;

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
    private StockStatus stockStatus;

    private Long categoryId;
    private Long brandId;

    private Set<String> tags;

    private Long viewCount;
    private Integer orderCount;
    private Double avgRating;

    private String searchKeywords;
    private Integer searchBoost;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;

    private List<ProductVariantDto> variants;
    private List<ProductAttributesDto> attributes;
    
}
