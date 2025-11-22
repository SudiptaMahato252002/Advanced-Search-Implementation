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
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
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

    public SearchResponse searchWithFuzzyRanking(String searchQuery,boolean enableFuzzySearch,int page,int size)
    {
        try {
            Long startTime=System.currentTimeMillis();
            Query query=enableFuzzySearch?buildEnhancedFuzzyQuery(searchQuery):buildMultiMatchQuery(searchQuery);
            Query rankQuery=buildFunctionScoreQuery(query);

            SearchRequest searchRequest=SearchRequest.of(s->s
                .index(INDEX_NAME)
                .query(rankQuery)
                .from(page*size)
                .size(size));
            
            var response=client.search(searchRequest, ProductDocument.class);
            List<ProductDocument> products=response.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
            Long took=System.currentTimeMillis()-startTime;
            Long total=response.hits().total().value();
            log.info("✅ Found {} results in {} ms (fuzzy: {})", total, took, enableFuzzySearch);
            return SearchResponse.builder()
                .query(searchQuery)
                .results(products)
                .total(total)
                .tookMs(took)
                .page(page)
                .size(size)
                .build();

        } catch (Exception e) {
            log.error("❌ Advanced search failed", e);
            throw new RuntimeException("Advanced search failed", e);
        }
    }
    
    public SearchResponse advancedSearch(
        String searchQuery,
            List<String> brandNames,
            String categoryName,
            Double minPrice,
            Double maxPrice,
            String stockStatus,
            Double minRating,
            boolean enableFuzzy,
            String sortBy,
            int page,
            int size
    )
    {
        try {
            Long startTime=System.currentTimeMillis();
            log.info("🔍 Advanced search - Query: '{}', Fuzzy: {}, Sort: {}", searchQuery, enableFuzzy, sortBy);

            if(searchQuery==null)
            {
                throw new Error("Please enter the query");
            }
            if(brandNames==null)
            {
                throw new Error("Please enter the brandnames");
            }
        

            BoolQuery.Builder boolQuery=new BoolQuery.Builder();

            if(searchQuery!=null||!searchQuery.trim().isEmpty())
            {
                Query query=enableFuzzy?buildEnhancedFuzzyQuery(searchQuery):buildMultiMatchQuery(searchQuery);
                boolQuery.must(query);
            }
            List<Query> filters=new ArrayList<>();

            if(brandNames!=null||!brandNames.isEmpty())
            {
                filters.add(Query.of(q->q
                    .terms(t->t
                        .field("brand_name")
                        .terms(terms->terms.value(brandNames.stream().map(b->FieldValue.of(b)).collect(Collectors.toList()))))));
            }
            if(categoryName!=null||!categoryName.trim().isEmpty())
            {
                filters.add(Query.of(q->q.term(t->t.field("category_name").value(categoryName))));
            }

            if(minPrice!=null||maxPrice!=null)
            {
                filters.add(Query.of(q->q
                    .range(r->
                    { 
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
            if(stockStatus!=null && !stockStatus.trim().isEmpty())
            {
                filters.add(Query.of(q->q.term(t->t.field("stock_status").value(stockStatus))));
            }
            
            if (minRating != null) 
            {
                filters.add(Query.of(q -> q
                    .range(r -> r
                        .field("avg_rating")
                        .gte(co.elastic.clients.json.JsonData.of(minRating))
                    )
                ));
            }

            if(!filters.isEmpty())
            {
                boolQuery.filter(filters);
            }
            Query baseQuery=Query.of(q->q.bool(boolQuery.build()));
            Query finalQuery=buildFunctionScoreQuery(baseQuery);

            SearchRequest.Builder searchRequestBuilder=new SearchRequest.Builder()
                .index(INDEX_NAME)
                .query(finalQuery)
                .from(page*size)
                .size(size);

            if(sortBy!=null && !sortBy.isEmpty())
            {
                addSorting(searchRequestBuilder, sortBy);
            }
            
            SearchRequest searchRequest=searchRequestBuilder.build();
            var response=client.search(searchRequest,ProductDocument.class);

            List<ProductDocument> products=response.hits().hits().stream().map(Hit::source).collect(Collectors.toList());

            Long took=System.currentTimeMillis()-startTime;
            Long total=response.hits().total().value();

            log.info("✅ Found {} filtered results in {} ms", total, took);

            return SearchResponse.builder()
                .query(searchQuery)
                .results(products)
                .query(searchQuery)
                .tookMs(took)
                .page(page)
                .size(size)
                .filters(filterSummary(brandNames, categoryName, minPrice, maxPrice, stockStatus))
                .build();

        } catch (Exception e) {
           log.error("❌ Advanced search failed", e);
            throw new RuntimeException("Advanced search failed", e);
        }
    }
 
    private Query buildEnhancedFuzzyQuery(String searchQuery)
    {
        int queryLength=searchQuery.length();
        String fuzziness=determineFuzziness(queryLength);

        return Query.of(q->q
            .bool(b->b
                .should(List.of(
                    Query.of(sq->sq
                        .multiMatch(mm->mm
                            .query(searchQuery)
                            .fields("name.exact^10", "brand_name^8")
                            .type(TextQueryType.Phrase)
                            .boost(10.0f))),
                    Query.of(sq->sq
                        .multiMatch(mm->mm
                            .query(searchQuery)
                            .fields("name^5", "short_description^3", "tags^3", "search_keywords^2")
                            .type(TextQueryType.BestFields)
                            .boost(5.0f))),
                    Query.of(sq->sq
                        .multiMatch(mm->mm
                            .query(searchQuery)
                            .fields("name.synonym^4", "short_description.synonym^2", "tags.synonym^3", "search_keywords.synonym^2")
                            .type(TextQueryType.BestFields)
                            .boost(4.0f))),
                    Query.of(sq->sq
                        .multiMatch(mm->mm
                            .query(searchQuery)
                            .fields("name.phonetic^3", "short_description.phonetic^1.5", "tags.phonetic^2", "search_keywords.phonetic^1.5", "brand_name.phonetic^2")
                            .type(TextQueryType.BestFields)
                            .boost(3.0f))),
                    Query.of(sq->sq
                        .multiMatch(mm->mm
                            .query(searchQuery)
                            .fields("name^3", "short_description^2", "tags^2", "search_keywords")
                            .type(TextQueryType.BestFields)
                            .fuzziness(fuzziness)
                            .prefixLength(0)
                            .maxExpansions(50)
                            .boost(2.5f)
                        )),
                    Query.of(sq->sq
                        .multiMatch(mm->mm
                            .query(searchQuery)
                            .fields("name.edge")
                            .query(searchQuery)
                            .boost(1.5f)
                        )),
                    Query.of(sq -> sq
                        .multiMatch(mm -> mm
                            .query(searchQuery)
                            .fields("full_description", "full_description.synonym")
                            .type(TextQueryType.BestFields)
                            .boost(1.0f)
                        )
                    )

                )).minimumShouldMatch("1")));
    }

    private String determineFuzziness(int queryLength)
    {
        if(queryLength<=2)
        {
            return "0";
        }
        else if(queryLength<=5)
        {
            return "2";
        }
        else
        {
            return "AUTO";
        }
    }
  
    private Query buildFunctionScoreQuery(Query baseQuery)
    {
        return Query.of(q->q
            .functionScore(fs->fs
                .query(baseQuery)
                .functions(List.of(
                    FunctionScore.of(fn->fn.fieldValueFactor(fvf->fvf
                        .field("view_count")
                        .factor(0.0002)
                        .modifier(FieldValueFactorModifier.Log1p)
                        .missing(1.0))
                        .weight(1.8)),
                    FunctionScore.of(fn->fn.fieldValueFactor(fvf->fvf
                        .field("order_count")
                        .factor(0.002)
                        .modifier(FieldValueFactorModifier.Log1p)
                        .missing(1.0))
                        .weight(3.0)),
                    FunctionScore.of(fn->fn.fieldValueFactor(fvf->fvf
                        .field("avg_rating")
                        .factor(0.2)
                        .modifier(FieldValueFactorModifier.None)
                        .missing(0.0))
                        .weight(2.0)),
                    FunctionScore.of(fn->fn.filter(f->f.term(t->t
                        .field("stock_status")
                        .value("IN_STOCK")))
                        .weight(2.5)),
                    FunctionScore.of(fn->fn.fieldValueFactor(fvf->fvf
                        .field("search_boost")
                        .factor(0.2)
                        .modifier(FieldValueFactorModifier.None)
                        .missing(1.0))
                        .weight(2.0)),
                    FunctionScore.of(fn->fn.gauss(g->g
                        .field("created_at")
                        .placement(p->p
                            .origin(JsonData.of("now"))
                            .scale(JsonData.of("365d"))
                            .decay(0.5)
                            ))
                            .weight(0.8))
                    
                ))
                .scoreMode(FunctionScoreMode.Sum)
                .boostMode(FunctionBoostMode.Multiply)
                .maxBoost(100.0)
                .minScore(0.1)));
    }

    private void addSorting(SearchRequest.Builder builder,String sortBy)
    {
        switch(sortBy.toLowerCase())
        {
            case "price_asc":
                builder.sort(s->s.field(f->f.field("current_price").order(SortOrder.Asc)));
                break;
            case "price_desc":
                builder.sort(s->s.field(f->f.field("current_price").order(SortOrder.Desc)));
                break;
            case "rating_desc":
                builder.sort(s->s.field(f->f.field("rating_desc").order(SortOrder.Desc)));
                break;
            case "popularity":
                builder.sort(s -> s.field(f -> f.field("view_count").order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)));
                break;
            case "newest":
                builder.sort(s -> s.field(f -> f.field("created_at").order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)));
                break;
            case "relevance":
            default:
                break;
        }
    }
}
