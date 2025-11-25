package com.DatabaseOperations.responses;

import java.util.List;

import com.DatabaseOperations.dtos.SuggestionDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoCompleteResponse 
{
    private String query;
    private List<SuggestionDto> suggestions;
    private int total;
    private Boolean cached;
    private Long tookMs;
    
}
