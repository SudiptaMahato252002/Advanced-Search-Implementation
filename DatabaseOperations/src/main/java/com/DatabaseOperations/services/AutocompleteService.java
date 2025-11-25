package com.DatabaseOperations.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.DatabaseOperations.dtos.SuggestionDto;
import com.DatabaseOperations.responses.AutoCompleteResponse;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AutocompleteService 
{
    @Autowired
    private RedisCacheService cacheService;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private static final String INDEX_NAME="products";
    private static final int MAX_SUGGESTIONS=10;
    private static final int MIN_QUERY_LENGTH=3;

    public AutoCompleteResponse getAutoCompleteSuggestion(String query)
    {
        long startTime=System.currentTimeMillis();
        if(query==null || query.trim().length()<MIN_QUERY_LENGTH)
        {
            return buildEmptyResponse(query, startTime);
        }

        String normalizedQuery=query.trim().toLowerCase();
        List<SuggestionDto> cachedSuggestions=cacheService.getSuggestions(normalizedQuery);

        if(cachedSuggestions!=null)
        {
            return AutoCompleteResponse.builder()
                .query(normalizedQuery)
                .suggestions(cachedSuggestions)
                .total(cachedSuggestions.size())
                .tookMs(System.currentTimeMillis()-startTime)
                .cached(true)    
                .build();
        }
        try 
        {
            List<SuggestionDto> queriedSuggestions=queryElasticSearch(normalizedQuery);
            cacheService.storeSuggestions(normalizedQuery, queriedSuggestions);
            long time=System.currentTimeMillis()-startTime;
            log.info("🔍 Autocomplete '{}': {} suggestions in {}ms", query, queriedSuggestions.size(), time);

            return AutoCompleteResponse.builder()
                .query(normalizedQuery)
                .suggestions(queriedSuggestions)
                .total(queriedSuggestions.size())
                .cached(true)
                .tookMs(time)
                .build();   
        } 
        catch (Exception e) 
        {
            log.error("Error in autocomplete for: '{}'", query, e);
            return buildEmptyResponse(query, startTime);
        }
    }

    private List<SuggestionDto> queryElasticSearch(String query)throws IOException
    {
        List<SuggestionDto> allSuggestions=new ArrayList<>();
        allSuggestions.addAll(getProductSuggestion(query, 6));
        allSuggestions.addAll(getBrandSuggestion(query, 3));
        allSuggestions.addAll(getCategorySuggestions(query, 1));
        return allSuggestions.stream().limit(MAX_SUGGESTIONS).collect(Collectors.toList());
    }
    private List<SuggestionDto> getProductSuggestion(String query,int limit)throws IOException
    {
        Query prefixQuery=Query.of(q->q
            .multiMatch(mm->mm
                .query(query)
                .fields("name.edge^3", "name^2","name.synonym^1.5")
                .type(TextQueryType.BestFields)
            ));
        
        BoolQuery boolQuery=BoolQuery.of(b->b
            .must(prefixQuery)
            .filter(Query.of(f->f.term(t->t.field("is_active").value(true))))
            .filter(Query.of(f->f.term(t->t.field("stock_status").value("IN_STOCK"))))
        );

        SearchRequest request=SearchRequest.of(s->s
            .index(INDEX_NAME)
            .query(Query.of(q->q.bool(boolQuery)))
            .source(src->src.filter(f->f.includes("id", "name", "current_price", "brand_name", "category_name", "view_count")))
            .sort(sort->sort.field(f->f.field("view_count").order(SortOrder.Desc)))
            .size(limit)
        );

        SearchResponse<Map> response=elasticsearchClient.search(request, Map.class);
        return response.hits().hits().stream().map(hit->maptToProductSuggestion(hit)).collect(Collectors.toList());

    }
    
    private SuggestionDto maptToProductSuggestion(Hit<Map> hit)
    {
        Map<String,Object> source=hit.source();
        return SuggestionDto.builder()
        .text((String)source.get("name"))
        .type("product")
        .id(((Number)source.get("id")).longValue())
        .score(hit.score())
        .brand(((String)source.get("brand_name")))
        .category(((String)source.get("category_name")))
        .price(source.get("current_price") != null ? ((Number) source.get("current_price")).doubleValue() : null)
        .inStock(true)
        .build();
    }

    private List<SuggestionDto> getBrandSuggestion(String query,int limit)throws IOException
    {
        Query prefixQuery=Query.of(q->q.multiMatch(mm->mm
            .query(query)
            .fields("brand_name^3", "brand_name.text^2")
            .type(TextQueryType.BestFields)
        ));

        BoolQuery boolQuery=BoolQuery.of(b->b
            .must(prefixQuery)
            .filter(f->f.term(t->t.field("is_active").value(true)))
            .filter(f->f.term(t->t.field("stock_status").value("IN_STOCK")))
        );

        SearchRequest request=SearchRequest.of(r->r
            .index(INDEX_NAME)
            .query(bq->bq.bool(boolQuery))
            .source(src->src.filter(f->f.includes("brand_id", "brand_name", "brand_is_popular")))
            .collapse(c->c.field("brand_name"))
            .sort(sort->sort.field(f->f.field("brand_is_popular").order(SortOrder.Desc)))
            .size(limit)
        );

        SearchResponse<Map> response=elasticsearchClient.search(request, Map.class);
        return response.hits().hits().stream().map(hit->maptToBrandSuggestion(hit)).collect(Collectors.toList());

    }

     private SuggestionDto maptToBrandSuggestion(Hit<Map> hit)
     {
        Map<String,Object> source=hit.source();
        return SuggestionDto.builder()
        .text((String)source.get("brand_name"))
        .type("brand")
        .id(((Number) source.get("brand_id")).longValue())
        .score(hit.score())
        .build();
     }

   private List<SuggestionDto> getCategorySuggestions(String query, int limit) throws IOException {
        Query categoryQuery = Query.of(q -> q
            .multiMatch(MultiMatchQuery.of(mm -> mm
                .query(query)
                .fields("category_name^2", "category_full_path")
                .type(TextQueryType.BestFields)
            ))
        );
        
        BoolQuery boolQuery = BoolQuery.of(b -> b
            .must(categoryQuery)
            .filter(Query.of(f -> f.term(t -> t.field("is_active").value(true))))
            .filter(Query.of(f -> f.term(t -> t.field("stock_status").value("IN_STOCK"))))
        );
        
        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index(INDEX_NAME)
            .query(Query.of(q -> q.bool(boolQuery)))
            .source(src -> src.filter(f -> f.includes("category_id", "category_name", "category_full_path")))
            .collapse(c -> c.field("category_name"))
            .size(limit)
        );
        
        SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);
        
        return response.hits().hits().stream()
            .map(hit -> mapToCategorySuggestion(hit))
            .collect(Collectors.toList());
    }
    
    private SuggestionDto mapToCategorySuggestion(Hit<Map> hit) {
        Map<String, Object> source = hit.source();
        return SuggestionDto.builder()
            .text((String) source.get("category_name"))
            .type("category")
            .id(((Number) source.get("category_id")).longValue())
            .score(hit.score())
            .category((String) source.get("category_full_path"))
            .build();
    }
    
    private AutoCompleteResponse buildEmptyResponse(String query,long startTime)
    {
        return AutoCompleteResponse.builder()
                .query(query)
                .suggestions(new ArrayList<>())
                .total(0)
                .cached(false)
                .tookMs(System.currentTimeMillis()-startTime)
                .build();
    }


}
