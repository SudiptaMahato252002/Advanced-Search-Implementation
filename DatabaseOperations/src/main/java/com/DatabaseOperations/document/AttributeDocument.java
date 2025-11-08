package com.DatabaseOperations.document;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeDocument 
{
     private Long id;
        
        @JsonProperty("attribute_name")
        private String attributeName;
        
        @JsonProperty("attribute_value")
        private String attributeValue;
        
        @JsonProperty("attribute_group")
        private String attributeGroup;
        
        private String unit;
        
        @JsonProperty("data_type")
        private String dataType;
        
        @JsonProperty("is_searchable")
        private Boolean isSearchable;
        
        @JsonProperty("is_filterable")
        private Boolean isFilterable;
    
}
