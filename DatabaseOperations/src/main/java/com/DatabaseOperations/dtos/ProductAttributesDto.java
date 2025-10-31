package com.DatabaseOperations.dtos;

import java.time.LocalDateTime;
import com.DatabaseOperations.enums.AttributeDataType;
import lombok.Data;

@Data
public class ProductAttributesDto {

    private Long id;

    private Long productId;

    private String attributeName;
    private String attributeValue;
    private String attributeGroup;

    private Integer displayOrder;
    private Boolean isSearchable;
    private Boolean isFilterable;

    private String unit;
    private AttributeDataType dataType;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}