package com.DatabaseOperations.mappers;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.DatabaseOperations.document.AttributeDocument;
import com.DatabaseOperations.document.ProductDocument;
import com.DatabaseOperations.document.VariantDocument;
import com.DatabaseOperations.entities.Product;
import com.DatabaseOperations.entities.ProductAttribute;
import com.DatabaseOperations.entities.ProductVariant;

@Component
public class ProductDocumentMapper 
{
    public ProductDocument toDocument(Product product)
    {
        if(product==null)
        {
            return null;
        }
        return ProductDocument.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .slug(product.getSlug())
                .shortDescription(product.getShortDescription())
                .fullDescription(product.getFullDescription())
                .isActive(product.getIsActive())
                .basePrice(product.getBasePrice())
                .discountedPrice(product.getDiscountedPrice())
                .discountedPercentage(product.getDiscountedPercentage())
                .currentPrice(product.getCurrentPrice())
                .currency(product.getCurrency())
                .stockQuantity(product.getStockQuantity())
                .stockStatus(product.getStockStatus().name())
                .brandId(product.getBrand()!=null?product.getBrand().getId():null)
                .brandName(product.getBrand()!=null?product.getBrand().getName():null)
                .brandSlug(product.getBrand()!=null?product.getBrand().getSlug():null)
                .brandIsPopular(product.getBrand()!=null?product.getBrand().getIsPopular():null)
                .categoryId(product.getCategory()!=null?product.getCategory().getId():null)
                .categoryName(product.getCategory()!=null?product.getCategory().getName():null)
                .categorySlug(product.getCategory()!=null?product.getCategory().getSlug():null)
                .categoryFullPath(product.getCategory()!=null?product.getCategory().getFullPath():null)
                .catgeoryLevel(product.getCategory()!=null?product.getCategory().getLevel():null)
                .tags(product.getTags())
                .searchKeywords(product.getSearchKeywords())
                .searchBoost(product.getSearchBoost())
                .viewCount(product.getViewCount())
                .orderCount(product.getOrderCount())
                .avgRating(product.getAvgRating())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .publishedAt(product.getPublishedAt())
                .variants(product.getVariants()!=null?product.getVariants().stream().map(this::toVariantDocument).collect(Collectors.toList()):null)
                .attributes(product.getAttributes() != null
                    ? product.getAttributes().stream()
                        .map(this::toAttributeDocument)
                        .collect(Collectors.toList())
                    : null)
                .build();
    }       
    public VariantDocument toVariantDocument(ProductVariant variant)
    {
        if(variant==null)
        {
            return null;
        }

        return VariantDocument.builder()
                            .id(variant.getId())
                            .sku(variant.getSku())
                            .variantName(variant.getVariantName())
                            .color(variant.getColor())
                            .size(variant.getSize())
                            .storage(variant.getStorage())
                            .material(variant.getMaterial())
                            .additionalPrice(variant.getAdditionalPrice())
                            .stockQuantity(variant.getStockQuantity())
                            .isAvailable(variant.getIsAvailable())
                            .isDefault(variant.getIsDefault())                                 
                            .build();
    }
    
    public AttributeDocument toAttributeDocument(ProductAttribute attribute)
    {
        if(attribute==null)
        {
            return null;
        }

        return AttributeDocument.builder()
                        .id(attribute.getId())
                        .attributeName(attribute.getAttributeName())
                        .attributeValue(attribute.getAttributeValue())
                        .attributeGroup(attribute.getAttributeGroup())
                        .unit(attribute.getUnit())
                        .dataType(attribute.getDataType().name())
                        .isSearchable(attribute.getIsSearchable())
                        .isFilterable(attribute.getIsFilterable())
                        .build();
    }
    
}
