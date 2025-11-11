package com.DatabaseOperations.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.DatabaseOperations.document.ProductDocument;
import com.DatabaseOperations.responses.SearchResponse;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SearchService 
{
    @Autowired
    private ElasticsearchClient client;
    private static final String INDEX_NAME="products";

    public SearchResponse searchProducts(String searchQuery,int page,int size)
    {

        try 
        {
            Long startTime=System.currentTimeMillis();
            log.info("🔍 Searching for: '{}' (page: {}, size: {})", searchQuery, page, size);

            Query query=buildMultiMatchQuery(searchQuery);
            SearchRequest searchRequest=SearchRequest.of(s->s
                        .index(INDEX_NAME)
                        .query(query)
                        .from(page*size)
                        .size(size));
            var response=client.search(searchRequest, ProductDocument.class);
            List<ProductDocument> products=response.hits().hits()
                                            .stream()
                                            .map(Hit::source)
                                            .collect(Collectors.toList());
            Long took=System.currentTimeMillis()-startTime;
            Long total=response.hits().total().value();

            return SearchResponse.builder()
                    .query(searchQuery)
                    .results(products)
                    .total(total)
                    .page(page)
                    .size(size)
                    .tookMs(took)
                    .build();
        } 
        catch (Exception e) 
        {
           log.error("❌ Search failed for query: '{}'", searchQuery, e);
            throw new RuntimeException("Search failed", e);
        }
    }
    public SearchResponse searchWithFilters(String searchQuery,List<String> brandNames,String categoryName,Double minPrice,Double maxPrice,String stockStatus,int page,int size)
    {
        try 
        {
            Long startTime=System.currentTimeMillis();
            log.info("🔍 Searching with filters - Query: '{}', Brands: {}, Category: {}, Price: {}-{}, Stock: {}", 
                searchQuery, brandNames, categoryName, minPrice, maxPrice, stockStatus);

            BoolQuery.Builder boolQuery=new BoolQuery.Builder();
            if(searchQuery!=null && !searchQuery.trim().isEmpty())
            {
                boolQuery.must(buildMultiMatchQuery(searchQuery));   
            }
            List<Query> filters=new ArrayList<>();

            if(brandNames!=null && !brandNames.isEmpty())
            {
                filters.add(Query.of(q->q
                    .terms(t->t
                    .field("brand_name")
                    .terms(terms->terms.value(
                        brandNames.stream()
                        .map(b->FieldValue.of(b))
                        .collect(Collectors.toList())
                    ))
                    )
                ));   
            }

            if(categoryName!=null && !categoryName.trim().isEmpty())
            {
                filters.add(Query.of(q->q
                .term(t->t
                    .field("category_name")
                    .value(categoryName)
                    )));
            }

            if(minPrice!=null ||maxPrice!=null)
            {
                filters.add(Query.of(q->q
                    .range(r->{
                        var rangeQuery=r.field("current_price");
                        if(minPrice!=null)
                        {
                            rangeQuery.gte(JsonData.of(minPrice));
                        }
                        if(maxPrice!=null)
                        {
                            rangeQuery.lte(JsonData.of(maxPrice));
                        }
                        return rangeQuery;
                    })));
            }

            if (stockStatus != null && !stockStatus.trim().isEmpty()) {
                filters.add(Query.of(q -> q
                    .term(t -> t
                        .field("stock_status")
                        .value(stockStatus)
                    )
                ));
            }

            if(!filters.isEmpty())
            {
                boolQuery.filter(filters);
            }
             Query finalQuery=Query.of(q->q.bool(boolQuery.build()));

            SearchRequest searchRequest=SearchRequest.of(s->s
                .index(INDEX_NAME)
                .query(finalQuery)
                .from(page*size)
                .size(size));
            
            var response=client.search(searchRequest, ProductDocument.class);

            List<ProductDocument> products=response.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
            
            Long took=System.currentTimeMillis()-startTime;
            Long total=response.hits().total().value();

            log.info("✅ Found {} filtered results in {} ms", total, took);
            
            return SearchResponse.builder()
                .query(searchQuery)
                .results(products)
                .total(total)
                .page(page)
                .size(size)
                .tookMs(took)
                .filters(filterSummary(brandNames, categoryName, minPrice, maxPrice, stockStatus))   
            .build();
        } 
        catch (Exception e) 
        {
            log.error("❌ Search with filters failed", e);
            throw new RuntimeException("Search with filters failed", e);
        }      
    }

    public Query buildMultiMatchQuery(String searchQuery)
    {
        Query query=Query.of(q->q
            .multiMatch(MultiMatchQuery.of(mm->mm
                .query(searchQuery)
                .fields("name^3", "short_description^2", "full_description", "tags^2", "search_keywords")
                .type(TextQueryType.BestFields))));
                
                return query;
    }
    private String filterSummary(List<String> brands, String category,Double minPrice, Double maxPrice, String stockStatus)
    {
        List<String> filters=new ArrayList<>();
        if (brands != null && !brands.isEmpty()) {
            filters.add("Brands: " + String.join(", ", brands));
        }
        if (category != null && !category.trim().isEmpty()) {
            filters.add("Category: " + category);
        }
        if (minPrice != null || maxPrice != null) {
            filters.add("Price: " + 
                (minPrice != null ? minPrice : "0") + " - " + 
                (maxPrice != null ? maxPrice : "∞"));
        }
        if (stockStatus != null && !stockStatus.trim().isEmpty()) {
            filters.add("Stock: " + stockStatus);
        }
        
        return filters.isEmpty() ? "No filters" : String.join(", ", filters);
    }
}
