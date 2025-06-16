package com.example.moviewatchlist.service;

import com.example.moviewatchlist.dto.TMDbSearchResponse;
import com.example.moviewatchlist.dto.TMDbImagesResponse;
import com.example.moviewatchlist.dto.TMDbSimilarResponse;
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
public class TMDbService {
    
    // Gets the API key from application.properties
    @Value("${tmdb.api.key}")
    private String apiKey;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public TMDbService() {
        // Set up HTTP client for making API calls
        this.httpClient = HttpClient.newHttpClient();
        // Set up JSON parser to convert responses into Java objects
        this.objectMapper = new ObjectMapper();
    }
    
    // Search for movies by title on TMDb
    public CompletableFuture<TMDbSearchResponse> searchMovie(String title) {
        // Build the search URL with API key and movie title
        String url = String.format("https://api.themoviedb.org/3/search/movie?api_key=%s&query=%s", 
                                 apiKey, title.replace(" ", "%20"));
        
        // Create the HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        
        // Send request asynchronously and parse the JSON response
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.body(), TMDbSearchResponse.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to parse TMDb search response", e);
                    }
                });
    }
    
    // Get movie poster and backdrop images from TMDb
    public CompletableFuture<TMDbImagesResponse> getMovieImages(Integer movieId) {
        // Build the images URL with the movie ID
        String url = String.format("https://api.themoviedb.org/3/movie/%d/images?api_key=%s", 
                                 movieId, apiKey);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        
        // Send request and convert response to Java object
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.body(), TMDbImagesResponse.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to parse TMDb images response", e);
                    }
                });
    }
    
    // Get movies similar to the given movie ID
    public CompletableFuture<TMDbSimilarResponse> getSimilarMovies(Integer movieId) {
        // Build the similar movies URL
        String url = String.format("https://api.themoviedb.org/3/movie/%d/similar?api_key=%s", 
                                 movieId, apiKey);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        
        // Get similar movies and parse the response
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.body(), TMDbSimilarResponse.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to parse TMDb similar movies response", e);
                    }
                });
    }
}