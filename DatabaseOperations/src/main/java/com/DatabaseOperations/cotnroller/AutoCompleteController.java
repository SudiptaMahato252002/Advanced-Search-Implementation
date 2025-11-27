package com.DatabaseOperations.cotnroller;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.DatabaseOperations.responses.AutoCompleteResponse;
import com.DatabaseOperations.services.AutocompleteService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/autocomplete")
public class AutoCompleteController 
{
    @Autowired
    private AutocompleteService service;

    @GetMapping
    public ResponseEntity<AutoCompleteResponse> getAutoComplete(@RequestParam(required = true) String q)
    {
        log.info("🔍 Autocomplete request for query: '{}'", q);
        
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if(q.trim().length()<3)
        {
            log.info("⚠️ Query too short: '{}'", q);
            AutoCompleteResponse emptyResponse= AutoCompleteResponse
                    .builder()
                    .query(q)
                    .suggestions(Collections.emptyList())
                    .cached(false)
                    .tookMs(0L)
                    .total(0)
                    .build();
            return new ResponseEntity<AutoCompleteResponse>(emptyResponse, HttpStatus.OK);
        }

        AutoCompleteResponse response=service.getAutoCompleteSuggestion(q);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
    
}
