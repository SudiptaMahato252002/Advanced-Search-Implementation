package com.DatabaseOperations.document;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantDocument 
{
    private Long id;
    private String sku;

    @JsonProperty("variant_name")
    private String variantName;

    private String color;
    private String size;
    private String storage;
    private String material;

    @JsonProperty("additional_price")
    private BigDecimal additionalPrice;
        
    @JsonProperty("stock_quantity")
    private Integer stockQuantity;
        
    @JsonProperty("is_available")
    private Boolean isAvailable;
        
    @JsonProperty("is_default")
    private Boolean isDefault;
    
}
