package com.DatabaseOperations.document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDocument 
{
    private Long id;
    private String sku;
    private String name;
    private String slug;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("short_description")
    private String shortDescription;
    @JsonProperty("ful_description")
    private String fullDescription;

    @JsonProperty("base_price")
    private BigDecimal basePrice;
    @JsonProperty("discounted_price")
    private BigDecimal discountedPrice;
    @JsonProperty("discounted_percentage")
    private Integer discountedPercentage;

    @JsonProperty("current_price")
    private BigDecimal currentPrice;
    
    private String currency;

    @JsonProperty("stock_quantity")  
    private Integer stockQuantity;
    @JsonProperty("stock_status")
    private String stockStatus;


    @JsonProperty("brand_id")
    private Long brandId;
    @JsonProperty("brenad_name")
    private String brandName;
    @JsonProperty("brand_slug")
    private String brandSlug;
    @JsonProperty("brand_is_popular")
    private Boolean brandIsPopular;

    @JsonProperty("category_id")
    private Long categoryId;
    @JsonProperty("category_name")
    private String categoryName;
    @JsonProperty("category_slug")
    private String categorySlug;
    @JsonProperty("category_full_path")
    private String categoryFullPath;
    @JsonProperty("category_level")
    private Integer catgeoryLevel;


    private Set<String> tags;
    @JsonProperty("search_keywords")
    private String searchKeywords;
    @JsonProperty("search_boost")
    private Integer searchBoost;
    @JsonProperty("view_count")
    private Long viewCount;
    @JsonProperty("order_count")
    private Integer orderCount;
    @JsonProperty("avg_rating")
    private Double avgRating;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    @JsonProperty("published_at")
    private LocalDateTime publishedAt;

    private List<VariantDocument> variants;
    private List<AttributeDocument> attributes;
}
