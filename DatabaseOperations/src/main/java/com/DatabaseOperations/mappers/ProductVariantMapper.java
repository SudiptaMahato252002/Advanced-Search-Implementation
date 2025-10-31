package com.DatabaseOperations.mappers;

import org.springframework.stereotype.Component;

import com.DatabaseOperations.dtos.ProductVariantDto;
import com.DatabaseOperations.entities.ProductVariant;

@Component
public class ProductVariantMapper 
{
    public ProductVariantDto objectToDto(ProductVariant variant)
    {
        if(variant==null)
        {
            return null;
        }
        ProductVariantDto variantDto=ProductVariantDto.builder()
                                    .id(variant.getId())
                                    .sku(variant.getSku())
                                    .variantName(variant.getVariantName())
                                    .color(variant.getColor())
                                    .size(variant.getSize())
                                    .storage(variant.getStorage())
                                    .size(variant.getSize())
                                    .material(variant.getMaterial())
                                    .additionalPrice(variant.getAdditionalPrice())
                                    .stockQuantity(variant.getStockQuantity())
                                    .isDefault(variant.getIsDefault())
                                    .displayOrder(variant.getDisplayOrder())
                                    .build();
        if (variant.getProduct() != null) {
        variantDto.setProductId(variant.getProduct().getId());
    }
    return variantDto;


    }
    
}
