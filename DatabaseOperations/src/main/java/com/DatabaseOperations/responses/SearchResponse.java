package com.DatabaseOperations.responses;

import java.util.List;

import com.DatabaseOperations.document.ProductDocument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse
{
    private String query;
    private List<ProductDocument> results;
    
    private Long total;
    private Integer page;
    private Integer size;

    private Long tookMs;
    private String filters;

    public Integer getTotalPages()
    {
        if(total==null||size==null||size==0)
        {
            return 0;
        }
        return (int)Math.ceil((double)total/size);


    }
    public boolean hasMore()
    {
        if(total==null||size==null||size==0)
        {
            return false;
        }
        return (page+1)*size<total;
    }
}