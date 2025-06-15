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
    
    @Value("${tmdb.api.key}")
    private String apiKey;
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public TMDbService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }
    
    public CompletableFuture<TMDbSearchResponse> searchMovie(String title) {
        String url = String.format("https://api.themoviedb.org/3/search/movie?api_key=%s&query=%s", 
                                 apiKey, title.replace(" ", "%20"));
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.body(), TMDbSearchResponse.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to parse TMDb search response", e);
                    }
                });
    }
    
    public CompletableFuture<TMDbImagesResponse> getMovieImages(Integer movieId) {
        String url = String.format("https://api.themoviedb.org/3/movie/%d/images?api_key=%s", 
                                 movieId, apiKey);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.body(), TMDbImagesResponse.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to parse TMDb images response", e);
                    }
                });
    }
    
    public CompletableFuture<TMDbSimilarResponse> getSimilarMovies(Integer movieId) {
        String url = String.format("https://api.themoviedb.org/3/movie/%d/similar?api_key=%s", 
                                 movieId, apiKey);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        
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