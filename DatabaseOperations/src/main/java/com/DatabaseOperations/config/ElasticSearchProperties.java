package com.DatabaseOperations.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix="elasticsearch")
@Getter
@Setter
public class ElasticSearchProperties 
{
    private String host;
    private int port;
    private String scheme;
    private int connectionTimeout;
    private int socketTimeout;
    private boolean enabled;
    
}
