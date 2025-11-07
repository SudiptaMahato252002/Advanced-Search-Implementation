package com.DatabaseOperations.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;

@Configuration
public class ElasticSearchConfig 
{
    @Autowired
    private ElasticSearchProperties elasticSearchProperties;

    @Bean
    public RestClient restClient()
    {
        System.out.println("Initializing elasticsearch RestClient");
        
        try 
        {
            HttpHost host=new HttpHost(
                elasticSearchProperties.getHost(),
                elasticSearchProperties.getPort(),
                elasticSearchProperties.getScheme()
            );

            RestClientBuilder builder=RestClient.builder(host);

            builder.setRequestConfigCallback(
                requestConfigBuilder->requestConfigBuilder.setConnectTimeout(elasticSearchProperties.getConnectionTimeout())
                                                        .setSocketTimeout(elasticSearchProperties.getSocketTimeout()));
            builder.setHttpClientConfigCallback(httpClientBuilder->httpClientBuilder.disableAuthCaching());

            RestClient client=builder.build();

            return client;

            
        } 
        catch (Exception e) 
        {
            System.out.println("❌ Failed to initialize Elasticsearch RestClient:"+e);
            throw new RuntimeException("Failed to initialize Elasticsearch RestClient", e);

        }
    }

    @Bean
    public ElasticsearchClient elasticSearchClient(RestClient restClient)
    {
        try 
        {
            RestClientTransport transport=new RestClientTransport(restClient,new JacksonJsonpMapper());
            ElasticsearchClient client=new ElasticsearchClient(transport);

            if(testConnection(client))
            {
                System.out.println("✅ Elasticsearch Client initialized and connected successfully");
            }
            else
            {
                System.out.println("⚠️ Elasticsearch Client initialized but connection test failed");
            }
            return client;
            
        } 
        catch (Exception e) 
        {
            System.out.println("❌ Failed to initialize Elasticsearch Client"+e);
            throw new RuntimeException("Failed to initialize Elasticsearch Client", e);
        }
    }
    
    private boolean testConnection(ElasticsearchClient client)
    {
        try 
        {
            boolean ping=client.ping().value();
            if(ping)
            {
                System.out.println("🔗 Successfully pinged Elasticsearch cluster");
                var info=client.info();
                System.out.println("📊 Elasticsearch Cluster Info:");
                System.out.println("   - Cluster Name: {}"+info.clusterName());
                System.out.println("   - Version: {}"+info.version().number());
                System.out.println("   - Node Name: {}"+ info.name());
                
            }
            return ping;
            
        } catch (Exception e) {
            System.out.println("Failed to ping Elasticsearch cluster"+e);
            return false;
        }
    }
}
