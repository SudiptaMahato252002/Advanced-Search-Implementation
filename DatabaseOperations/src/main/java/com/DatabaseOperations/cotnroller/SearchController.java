package com.DatabaseOperations.cotnroller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.DatabaseOperations.responses.SearchResponse;
import com.DatabaseOperations.services.SearchService;


import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/search")
@Slf4j
public class SearchController 
{
    @Autowired
    private SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResponse> search(
        @RequestParam(required = true)String q,
        @RequestParam(defaultValue = "0")int page,
        @RequestParam(defaultValue = "10")int size)
    {
        log.info("🔍 Search request - Query: '{}', Page: {}, Size: {}", q, page, size);
        
        if(q==null||q.trim().isEmpty())
        {
            log.warn("⚠️ Empty search query");
            return ResponseEntity.badRequest().build();
        }
        if(page<0)
        {
            page=0;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        SearchResponse response=searchService.searchProducts(q, page, size);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @GetMapping("/filter")
    public ResponseEntity<SearchResponse> searchWithFilter(
        @RequestParam(required=false) String q,
        @RequestParam(required=false)List<String> brand,
        @RequestParam(required=false)String category,
        @RequestParam(required=false) Double minPrice,
        @RequestParam(required=false)Double maxPrice,
        @RequestParam(required = false)String stock,
        @RequestParam(defaultValue = "0")int page,
        @RequestParam(defaultValue = "10")int size        
    )
    {
        log.info("🔍 Filtered search - Query: '{}', Brands: {}, Category: {}, Price: {}-{}, Stock: {}", 
            q, brand, category, minPrice, maxPrice, stock);
        
        if(page<0)
        {
            page=0;
        }
        if(size<1||size>100)
        {
            size=10;
        }
        if(minPrice!=null && minPrice<0)
        {
            minPrice=0.0;
        }
        if (maxPrice != null && maxPrice < 0) 
        {
            maxPrice = null;
        }
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) 
        {
            Double temp = minPrice;
            minPrice = maxPrice;
            maxPrice = temp;
        }
        SearchResponse response=searchService.searchWithFilters(q, brand, category, minPrice, maxPrice, stock, page, size);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/by-brand")
    public ResponseEntity<SearchResponse> searchByBrand(
        @RequestParam(required = true) String brand,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    )
    {
        log.info("🔍 Search by brand: '{}'", brand);
        SearchResponse response=searchService.searchWithFilters(null, List.of(brand), null, null, null, null, page, size);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @GetMapping("/by-category")
    public ResponseEntity<SearchResponse> searchByCategory(
        @RequestParam(required = true) String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    )
    {
        log.info("🔍 Search by category: '{}'", category);
        SearchResponse response=searchService.searchWithFilters(null, null, category, null, null, null, page, size);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @GetMapping("/by-price")
    public ResponseEntity<SearchResponse> searchByPrice(
        @RequestParam(required = true) Double minPrice,
        @RequestParam(required = true) Double maxPrice,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    )
    {
        
        log.info("🔍 Search by price: {} - {}", minPrice, maxPrice);
        SearchResponse response=searchService.searchWithFilters(null, null, null, minPrice, maxPrice, null, page, size);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @GetMapping("/advanced")
    public ResponseEntity<SearchResponse> advancedSearch(
        @RequestParam(required = false) String q,
            @RequestParam(required = false) List<String> brand,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String stock,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "false") boolean fuzzy,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    )
    {
        log.info("🚀 Advanced search - Query: '{}', Fuzzy: {}, Sort: {}", q, fuzzy, sortBy);
        if(page<0)
        {
            page=0;
        }
        if(size<0||size>100)
        {
            size=10;
        }
        if(minPrice!=null && minPrice<0)
        {
            minPrice=0.0;
        }
        if (maxPrice != null && maxPrice < 0) 
        {
            maxPrice = null;
        }
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) 
        {
            Double temp = minPrice;
            minPrice = maxPrice;
            maxPrice = temp;
        }
        if(minRating!=null &&(minRating<0||minRating>5))
        {
            minRating=null;
        }
        SearchResponse response=searchService.advancedSearch(q, brand, category, minPrice, maxPrice, stock, minRating, fuzzy, sortBy, page, size);
        return new ResponseEntity<SearchResponse>(response, HttpStatus.OK);
    }

    @GetMapping("/fuzzy")
    public ResponseEntity<SearchResponse> fuzzySearch(
        @RequestParam(required = true) String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    )
    {
        log.info("🔍 Fuzzy search: '{}'", q);
        
        if (page < 0) page = 0;
        if (size < 1 || size > 100) size = 20;

        SearchResponse response=searchService.searchWithFuzzyRanking(q, true, page, size);
        return new ResponseEntity<SearchResponse>(response,HttpStatus.OK);
    }

    private String buildFilterWithSummary(List<String> brands, String category, 
                                       Double minPrice, Double maxPrice, String stockStatus)
    {
        List<String> filters=new ArrayList<>();
        if(brands!=null && !brands.isEmpty())
        {
            filters.add("Brands: "+String.join(",",brands));
        }
        if (category != null && !category.trim().isEmpty()) {
            filters.add("Category: " + category);
        }
        if(minPrice!=null||maxPrice!=null)
        {
            filters.add("Price: "+(minPrice!=null?minPrice:"0")+" - "+(maxPrice!=null?maxPrice:"∞"));
        }
        if (stockStatus != null && !stockStatus.trim().isEmpty()) {
            filters.add("Stock: " + stockStatus);
        }

         return filters.isEmpty() ? "No filters" : String.join(", ", filters);
    }
    
}
