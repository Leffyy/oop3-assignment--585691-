package com.example.moviewatchlist.service;

import com.example.moviewatchlist.dto.OMDbResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

@Service
public class OMDbService {
    
    @Value("${omdb.api.key}")
    private String apiKey;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public OMDbService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }
    
    public CompletableFuture<OMDbResponse> getMovieData(String title) {
        String url = String.format("http://www.omdbapi.com/?t=%s&apikey=%s", 
                                 title.replace(" ", "+"), apiKey);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.body(), OMDbResponse.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to parse OMDb response", e);
                    }
                });
    }
}