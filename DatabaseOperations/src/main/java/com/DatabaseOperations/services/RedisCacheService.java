package com.DatabaseOperations.services;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.DatabaseOperations.dtos.SuggestionDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RedisCacheService 
{

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String AUTOCOMPLETE_PREFIX="autocomplet:";
    private static final long TTL_HOURS=1;

    public String generatedKey(String query)
    {
        return AUTOCOMPLETE_PREFIX+query.trim().toLowerCase();
    }

    public List<SuggestionDto> getSuggestions(String query)
    {
        try 
        {
            String key=generatedKey(query);
            Object cached=redisTemplate.opsForValue().get(key);
            if(cached!=null)
            {
                 log.info("✅ Cache HIT for query: '{}'", query);
                List<SuggestionDto> suggestions=objectMapper.convertValue(cached, new TypeReference<List<SuggestionDto>>(){});
                return suggestions;
            }
            log.info("❌ Cache MISS for query: '{}'", query);
            return null;
        } 
        catch (Exception e) 
        {
            log.error("Error retrieving from cache for query: '{}'", query, e);
            return null;
        }
    }

    public void storeSuggestions(String query,List<SuggestionDto> suggestions)
    {
        try 
        {
            String key=generatedKey(query);
            redisTemplate.opsForValue().set(key, suggestions, TTL_HOURS, TimeUnit.HOURS);
            log.info("💾 Stored {} suggestions in cache for query: '{}'", suggestions.size(), query);
            
        } 
        catch (Exception e) 
        {
            log.error("Error storing in cache for query: '{}'", query, e);
        }
    }
    public void invalidateAllAutocomplete()
    {
        try 
        {

            Set<String> keys=redisTemplate.keys(AUTOCOMPLETE_PREFIX+"*");
            redisTemplate.delete(keys);
            log.info("🗑️ Invalidated {} autocomplete cache entries", keys.size());
            
        } catch (Exception e) {
              log.error("Error invalidating autocomplete cache", e);
        }
    }
    public void invalidateQuery(String query) {
        try {
            String key = generatedKey(query);
            redisTemplate.delete(key);
            log.info("🗑️ Invalidated cache for query: '{}'", query);
        } catch (Exception e) {
            log.error("Error invalidating cache for query: '{}'", query, e);
        }
    }


    public boolean isConnected()
    {
        try 
        {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
            
        } 
        catch (Exception e) 
        {
            log.error("Redis connection failed", e);
            return false;
        }
    }
    
}
