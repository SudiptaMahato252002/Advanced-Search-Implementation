package com.DatabaseOperations.cotnroller;

import java.util.HashMap;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

@RestController
@RequestMapping("/api/elasticsearch")
public class ElasticSearchHealthController 
{
    @Autowired
    private ElasticsearchClient client;
    
    @GetMapping("/health")
    public ResponseEntity<Map<String,Object>> checkHealth()
    {
         Map<String,Object> result=new HashMap<>();
        try 
        {
           
            boolean isConnected=client.ping().value();
            if(isConnected)
            {
                var info=client.info();
                result.put("status","UP");
                result.put("clustername", info.clusterName());
                result.put("version", info.version().number());
                result.put("nodeName",info.name());

                return new ResponseEntity<>(result,HttpStatus.OK);
            }
            else{
                result.put("status","DOWN");
                result.put("message", "Unable to ping Elasticsearch");
                return new ResponseEntity<>(result,HttpStatus.SERVICE_UNAVAILABLE);
            }
            
        } 
        catch (Exception e) 
        {
            result.put("status", "ERROR");
            result.put("message",e.getMessage());
            return new ResponseEntity<>(result,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
    }
    @GetMapping("/cluster-info")
    public ResponseEntity<Map<String,Object>> getClusterInfo()
    {
        Map<String,Object> response=new HashMap<>();
        try 
        {
            var info=client.info();
            var health=client.cluster().health();

            response.put("cluster-name",info.clusterName());
            response.put("cluster-uuid",info.clusterUuid());
            response.put("node-name",info.name());
            response.put("version",info.version());
            response.put("lucene-version",info.version().luceneVersion());
            response.put("health-status",health.status().toString());
            response.put("number-of-nodes", health.numberOfNodes());
            response.put("number-of-data-nodes",health.numberOfDataNodes());
            response.put("active-primary-shards",health.activePrimaryShards());
            response.put("active-shards",health.activeShards());

            return new ResponseEntity<>(response,HttpStatus.OK);

            
        } 
        catch (Exception e) 
        {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            
            System.out.println("Failed to fetch cluster info"+e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
     @GetMapping("/indices")
    public ResponseEntity<Map<String,Object>> getIndices()
    {
        Map<String,Object> response=new HashMap<>();
        try 
        {
            var indices=client.cat().indices().indices();
             response.put("count", indices.size());
            response.put("indices", indices.stream()
                .map(index -> Map.of(
                    "index", index.index(),
                    "health", index.health(),
                    "status", index.status(),
                    "docsCount", index.docsCount() != null ? index.docsCount() : 0,
                    "storeSize", index.storeSize() != null ? index.storeSize() : "0b"
                ))
                .toList());
            return new ResponseEntity<>(response,HttpStatus.OK);
        } 
        catch (Exception e) 
        {
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            System.out.println("Failed to list indices"+e);
            return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


}
