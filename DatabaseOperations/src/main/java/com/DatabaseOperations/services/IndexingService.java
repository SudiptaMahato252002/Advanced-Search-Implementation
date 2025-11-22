package com.DatabaseOperations.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.DatabaseOperations.document.ProductDocument;
import com.DatabaseOperations.entities.Product;
import com.DatabaseOperations.mappers.ProductDocumentMapper;
import com.DatabaseOperations.repositories.ProductRepository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.analysis.PhoneticEncoder;
import co.elastic.clients.elasticsearch._types.mapping.BooleanProperty;
import co.elastic.clients.elasticsearch._types.mapping.DateProperty;
import co.elastic.clients.elasticsearch._types.mapping.DoubleNumberProperty;
import co.elastic.clients.elasticsearch._types.mapping.IntegerNumberProperty;
import co.elastic.clients.elasticsearch._types.mapping.KeywordProperty;
import co.elastic.clients.elasticsearch._types.mapping.LongNumberProperty;
import co.elastic.clients.elasticsearch._types.mapping.NestedProperty;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TextProperty;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class IndexingService 
{
    @Autowired
    private ElasticsearchClient client;
    @Autowired
    private ProductRepository productRepo;
    @Autowired
    private ProductDocumentMapper productDocumentMapper;

    private static final String INDEX_NAME="products";

    public boolean createProductIndex()
    {
        try 
        {
            boolean exists=client.indices().exists(ExistsRequest.of(e->e.index(INDEX_NAME))).value();
            if(exists)
            {
                System.out.println("Index '{}' already exist"+INDEX_NAME);
                return false;
            }
            CreateIndexRequest createRequest=CreateIndexRequest.of(c->
                c.index(INDEX_NAME)
                .settings(s->s.numberOfShards("1")
                                .numberOfReplicas("0")
                                .refreshInterval(t->t.time("1s"))
                                .analysis(a->a
                                    .filter("synonym_filter", f->f.definition(fd->fd
                                        .synonym(syn->syn.synonyms(List.of(
                                            "shoe, sneaker, footwear, kicks",
                                            "laptop, notebook, computer",
                                            "phone, mobile, smartphone, cellphone, fone",
                                            "tv, television, tele",
                                            "headphone, earphone, earbuds, headset"
                                        )))))
                                    .filter("phonetic_fitler", f->f.definition(fd->fd
                                        .phonetic(ph->ph
                                            .encoder(PhoneticEncoder.DoubleMetaphone)
                                            .replace(false))))
                                    .filter("edge_ngram_filter",f->f.definition(fd->fd
                                        .edgeNgram(en->en
                                            .minGram(2)
                                            .maxGram(15)
                                    )))
                                    .analyzer("synonym_analyzer",an->an.custom(ca->ca
                                        .tokenizer("standard")
                                        .filter("lowercase","synonym_filter")
                                    ))
                                    .analyzer("phonetic_analyzer",an->an.custom(ca->ca
                                        .tokenizer("standard")
                                        .filter("lowercase", "phonetic_filter")
                                    ))
                                    .analyzer("edge_ngram_analyzer", an->an.custom(ca->ca
                                        .tokenizer("standard")
                                        .filter("lowercase", "edge_ngram_filter")
                                    ))
                                
                                )
                            )
                                
                .mappings(m->m
                    .properties("id",Property.of(p->p.long_(LongNumberProperty.of(l->l))))
                    .properties("sku",Property.of(p->p.keyword(KeywordProperty.of(k->k))))
                    .properties("name",Property.of(p->p.text(TextProperty.of(t->t
                        .analyzer("standard")
                        .fields(Map.of(
                            "exact",Property.of(sp->sp.keyword(KeywordProperty.of(k->k))),
                            "synonym",Property.of(sp->sp.text(TextProperty.of(tp->tp.analyzer("synonym_analyzer")))),
                            "phonetic",Property.of(sp->sp.text(TextProperty.of(tp->tp.analyzer("phonetic_analyzer")))),
                            "edge",Property.of(sp->sp.text(TextProperty.of(tp->tp.analyzer("edge_ngram_analyzer"))))
                        ))

                ))))
                    .properties("slug",Property.of(p->p.keyword(KeywordProperty.of(k->k))))
                    .properties("short_description",Property.of(p->p.text(TextProperty.of(t->t.fields(Map.of(
                        "synonym",Property.of(sp->sp.text(TextProperty.of(tp->tp.analyzer("synonym_analyzer"))))
                    ))))))
                    .properties("full_description",Property.of(p->p.text(TextProperty.of(t->t.fields(Map.of(
                        "synonym",Property.of(sp->sp.text(TextProperty.of(tp->tp.analyzer("synonym_analyzer"))))
                    ))))))
                    .properties("is_active",Property.of(p->p.boolean_(BooleanProperty.of(b->b))))

                    .properties("base_price", Property.of(p -> p.double_(DoubleNumberProperty.of(d -> d))))
                    .properties("discounted_price", Property.of(p -> p.double_(DoubleNumberProperty.of(d -> d))))
                    .properties("discounted_percentage", Property.of(p -> p.integer(IntegerNumberProperty.of(i -> i))))
                    .properties("current_price", Property.of(p -> p.double_(DoubleNumberProperty.of(d -> d))))
                    .properties("currency", Property.of(p -> p.keyword(KeywordProperty.of(k -> k))))
                    
                    // Stock
                    .properties("stock_quantity", Property.of(p -> p.integer(IntegerNumberProperty.of(i -> i))))
                    .properties("stock_status", Property.of(p -> p.keyword(KeywordProperty.of(k -> k))))
                    
                    // Brand
                    .properties("brand_id", Property.of(p -> p.long_(LongNumberProperty.of(l -> l))))
                    .properties("brand_name", Property.of(p -> p.keyword(KeywordProperty.of(k -> k.fields(Map.of(
                        "text",Property.of(sp->sp.text(TextProperty.of(tp->tp))),
                        "phonetic",Property.of(sp->sp.text(TextProperty.of(tp->tp.analyzer("ponetic_analyzer"))))
                    ))))))
                    .properties("brand_slug", Property.of(p -> p.keyword(KeywordProperty.of(k -> k))))
                    .properties("brand_is_popular", Property.of(p -> p.boolean_(BooleanProperty.of(b -> b))))
                    
                    // Category
                    .properties("category_id", Property.of(p -> p.long_(LongNumberProperty.of(l -> l))))
                    .properties("category_name", Property.of(p -> p.keyword(KeywordProperty.of(k -> k))))
                    .properties("category_slug", Property.of(p -> p.keyword(KeywordProperty.of(k -> k))))
                    .properties("category_full_path", Property.of(p -> p.text(TextProperty.of(t -> t))))
                    .properties("category_level", Property.of(p -> p.integer(IntegerNumberProperty.of(i -> i))))
                    
                    // Search fields
                    .properties("tags", Property.of(p -> p.keyword(KeywordProperty.of(k -> k.fields(Map.of(
                        "text",Property.of(sp->sp.text(TextProperty.of(tp->tp))),
                        "synonym",Property.of(sp->sp.text(TextProperty.of(tp->tp.analyzer("synonym_analyzer")))),
                        "phonetic", Property.of(sp -> sp.text(TextProperty.of(tp -> tp.analyzer("phonetic_analyzer"))))
                    ))))))
                    .properties("search_keywords", Property.of(p -> p.text(TextProperty.of(t -> t
                        .fields(Map.of(
                            "synonym", Property.of(sp -> sp.text(TextProperty.of(tp -> tp.analyzer("synonym_analyzer")))),
                            "phonetic", Property.of(sp -> sp.text(TextProperty.of(tp -> tp.analyzer("phonetic_analyzer"))))
                        ))
                    ))))
                    .properties("search_boost", Property.of(p -> p.integer(IntegerNumberProperty.of(i -> i))))
                    
                    // Metrics
                    .properties("view_count", Property.of(p -> p.long_(LongNumberProperty.of(l -> l))))
                    .properties("order_count", Property.of(p -> p.integer(IntegerNumberProperty.of(i -> i))))
                    .properties("avg_rating", Property.of(p -> p.double_(DoubleNumberProperty.of(d -> d))))
                    
                    // Timestamps
                    .properties("created_at", Property.of(p -> p.date(DateProperty.of(d -> d))))
                    .properties("updated_at", Property.of(p -> p.date(DateProperty.of(d -> d))))
                    .properties("published_at", Property.of(p -> p.date(DateProperty.of(d -> d))))
  
                    .properties("variants",Property.of(p->p.nested(NestedProperty.of(n->n))))
                    .properties("attributes",Property.of(p->p.nested(NestedProperty.of(n->n))))
                    )                
                );
                var response=client.indices().create(createRequest);
                return response.acknowledged();
            
        } catch (Exception e) {
          
            throw new RuntimeException("Failed to create index", e);
        }
    }
    public boolean deleteProductIndex()
    {
        try 
        {
            boolean exists=client.indices()
                .exists(ExistsRequest.of(e->e.index(INDEX_NAME))).value();
            if(!exists)
            {
                System.out.println("Index '{}' does not exist"+ INDEX_NAME);
                return false;
            }
            DeleteIndexRequest request=DeleteIndexRequest.of(d->d.index(INDEX_NAME));
            var response=client.indices().delete(request);
            System.out.println("✅ Index '{}' deleted successfully"+INDEX_NAME);
            return response.acknowledged();
            
        } catch (Exception e) {
 
            System.out.println("❌ Failed to delete index '" + INDEX_NAME + "' " + e.getMessage());
            throw new RuntimeException("Failed to delete index", e);
        }
    }

    @Transactional(readOnly = true)
    public int indexAllProducts()
    {
        try 
        {
            log.info("Inside the indexing service");
            List<Product> products=productRepo.findAll();
            int len=products.size();
            log.info("Retrived produts from the repos '{}'",len);
            if(products.isEmpty())
            {
                return 0;
            }
            log.info("Starting to convert to Product Documents ");
            List<ProductDocument> documents=products.stream().map(productDocumentMapper::toDocument).collect(Collectors.toList());
            log.info("Starting bulk indexing");
            int indexed=bulkIndexDocuments(documents);

            return indexed;
        
        } 
        catch (Exception e) 
        {
          
              throw new RuntimeException("Failed to index all products", e);
        }

    }
    @Transactional(readOnly = true)
    public boolean singleIndexProduct(Long productId)
    {
        try {
            Product product=productRepo.findById(productId).orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
            ProductDocument document=productDocumentMapper.toDocument(product);
            client.index(i->i.index(INDEX_NAME).id(String.valueOf(document.getId())).document(document));
            return true;
        } 
        catch (IOException e) 
        {
            throw new RuntimeException("Failed to index product", e);
        }

    }

    private int bulkIndexDocuments(List<ProductDocument> documents)throws IOException
    {
        if(documents.isEmpty())
        {
            return 0;
        }
        log.info("starting the bulk operations");
        List<BulkOperation> bulkOperations=documents.stream()
                    .map(doc->BulkOperation.of(b->b.index(idx->idx.index(INDEX_NAME).id(String.valueOf(doc.getId())).document(doc)))).collect(Collectors.toList());
        log.info("Sending the bulk request");
        BulkRequest bulkRequest=BulkRequest.of(b->b.operations(bulkOperations));
        BulkResponse response=client.bulk(bulkRequest);
        
        if(response.errors())
        {
            response.items().forEach(item->{if(item.error()!=null){
                System.out.println("Failed to index document ID"+item.id()+" "+item.error().reason());
            }});
        }

        int successCount=(int)response.items().stream()
            .filter(item->item.error()==null).count();
    
        return successCount;
    }

    public boolean indexExists()
    {
        try {
            return client.indices().exists(ExistsRequest.of(e->e.index(INDEX_NAME))).value();
        } 
        catch (IOException e) 
        {
            System.out.println("Failed to check if index exists "+e);
            return false;
        }
    }

    public long getDocumentCount()
    {
        try 
        {
            return client.count(c->c.index(INDEX_NAME)).count();            
        } catch (IOException e) {
            System.out.println("Failed to get document count"+e);
            return 0;
        }
    }
}
