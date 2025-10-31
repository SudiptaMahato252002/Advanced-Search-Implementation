package com.DatabaseOperations.dtos;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CategoryDto 
{
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Integer level;
    private String fullPath;
    private Integer productCount;

    private Long parentId; // Only store parent ID to avoid recursion

    @Builder.Default
    private List<CategoryDto> subCategories = new ArrayList<>();
    
}
