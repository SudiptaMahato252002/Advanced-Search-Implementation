package com.DatabaseOperations.requests;

import com.DatabaseOperations.enums.AttributeDataType;

import lombok.Data;

@Data
public class ProductAttributesRequest 
{
    private Long productId;

    private String attributeName;
    private String attributeValue;
    private String attributeGroup;

    private Integer displayOrder;
    private Boolean isSearchable;
    private Boolean isFilterable;

    private String unit;
    private AttributeDataType dataType;
    
}
