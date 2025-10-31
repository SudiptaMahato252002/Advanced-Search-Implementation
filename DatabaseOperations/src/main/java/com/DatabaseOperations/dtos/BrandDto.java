package com.DatabaseOperations.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandDto 
{
     private Long id;
    private String name;
    private String slug;
    private String description;
    private Boolean isPopular;
    private Integer displayOrder;
    private Boolean isActive;
    private Integer productCount;
    private Double averageRating;
    private String country;
    private Integer foundedYear;
    
}
