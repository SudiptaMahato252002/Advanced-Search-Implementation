package com.DatabaseOperations.responses;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductVariantResponse {
    private Long id;
    private String variantName;
    private BigDecimal finalPrice;
    private Boolean isAvailable;
    private Integer stockQuantity;
}
