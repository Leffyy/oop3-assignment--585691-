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
    
    // This gets the API key from application.properties
    @Value("${omdb.api.key}")
    private String apiKey;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public OMDbService() {
        // Create an HTTP client to make requests
        this.httpClient = HttpClient.newHttpClient();
        // Create a JSON parser to convert responses to objects
        this.objectMapper = new ObjectMapper();
    }
    
    public CompletableFuture<OMDbResponse> getMovieData(String title) {
        // Build the URL with the movie title and API key
        // Replace spaces with + signs for URL encoding
        String url = String.format("https://www.omdbapi.com/?t=%s&apikey=%s", 
                                 title.replace(" ", "+"), apiKey);
        
        // Create the HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        
        // Send the request asynchronously and process the response
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        // Debug: Print the actual response
                        System.out.println("OMDb Response: " + response.body());
                        // Convert the JSON response to an OMDbResponse object
                        return objectMapper.readValue(response.body(), OMDbResponse.class);
                    } catch (IOException e) {
                        // error logging
                        System.out.println("Parsing error: " + e.getMessage());
                        System.out.println("Response body: " + response.body());
                        throw new RuntimeException("Failed to parse OMDb response: " + e.getMessage(), e);
                    }
                });
    }
}