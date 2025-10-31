package com.DatabaseOperations.responses;

import lombok.Data;

@Data
public class ProductAttributeResponse 
{
     private String attributeName;
    private String formattedValue;
    private String attributeGroup;
    private Boolean isFilterable;
    
}
