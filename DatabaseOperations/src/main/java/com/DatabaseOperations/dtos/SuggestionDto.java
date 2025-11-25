package com.DatabaseOperations.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionDto 
{
    private String text;
    private String type;
    private Long id;
    private Double score;

    private String brand;
    private String category;
    private Double price;
    private Boolean inStock;
    
}
