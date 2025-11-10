package com.DatabaseOperations.cotnroller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.DatabaseOperations.services.IndexingService;

@RestController
@RequestMapping("/api/indexing")
public class IndexingController 
{
    @Autowired
    private IndexingService indexingService;

    @PostMapping("/create-index")
    public ResponseEntity<Map<String,Object>> createIndex()
    {
        Map<String,Object> response=new HashMap<>();
        try {
            boolean created=indexingService.createProductIndex();
            if(created)
            {
                response.put("status", "SUCCESS");
                response.put("message", "Index created successfully");
            }
            else
            {
                 response.put("status", "WARNING");
                response.put("message", "Index already exists");
            }
            return new ResponseEntity<>(response,HttpStatus.OK);
        } catch (Exception e) {
            
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
    }

    @DeleteMapping("/delete-index")
    public ResponseEntity<Map<String,Object>> deleteIndex()
    {
        Map<String,Object> response=new HashMap<>();
        try 
        {
            boolean deleted=indexingService.deleteProductIndex();
             if (deleted) {
                response.put("status", "SUCCESS");
                response.put("message", "Index deleted successfully");
            } else {
                response.put("status", "WARNING");
                response.put("message", "Index does not exist");
            }
            return new ResponseEntity<>(response,HttpStatus.OK);
            
        } catch (Exception e) {

            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/index-all")
    public ResponseEntity<Map<String,Object>> indexAllProducts()
    {
        Map<String,Object> response=new HashMap<>();
        try 
        {
            long startTime=System.currentTimeMillis();
            int indexed=indexingService.indexAllProducts();
            long endTime=System.currentTimeMillis();
            long duration = endTime - startTime;
            
            response.put("status", "SUCCESS");
            response.put("message", "Products indexed successfully");
            response.put("indexed_count", indexed);
            response.put("duration_ms", duration);

            return new ResponseEntity<>(response,HttpStatus.OK);
            
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/index-produxt/{id}")
    public ResponseEntity<Map<String,Object>> indexSingleProduct(@PathVariable Long id)
    {
        Map<String,Object> response=new HashMap<>();
        try 
        {
            boolean indexed=indexingService.singleIndexProduct(id);
            if (indexed) {
                response.put("status", "SUCCESS");
                response.put("message", "Product indexed successfully");
                response.put("product_id", id);
            } else {
                response.put("status", "WARNING");
                response.put("message", "Product not indexed");
            }
            return new ResponseEntity<>(response,HttpStatus.OK);
            
        } catch (Exception e) {
            
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String,Object>> getIndexStatus()
    {
        Map<String,Object> response=new HashMap<>();
        try {
            boolean exists=indexingService.indexExists();
            long count=exists?indexingService.getDocumentCount():0;
            response.put("index_exists", exists);
            response.put("document_count", count);
            response.put("index_name", "products");
            return new ResponseEntity<>(response,HttpStatus.OK);    
        } 
        catch (Exception e) 
        {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/reindex-all")
    public ResponseEntity<Map<String,Object>> reindexAll()
    {
        Map<String, Object> response = new HashMap<>();
        try {

            if(indexingService.indexExists())
            {
                indexingService.deleteProductIndex();
            }
            
            indexingService.createProductIndex();
            int indexed=indexingService.indexAllProducts();

            response.put("status", "SUCCESS");
            response.put("message", "Reindexing completed successfully");
            response.put("indexed_count", indexed);
             return new ResponseEntity<>(response,HttpStatus.OK);    
        } 
        catch (Exception e) 
        {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);            
        }
    }
}
