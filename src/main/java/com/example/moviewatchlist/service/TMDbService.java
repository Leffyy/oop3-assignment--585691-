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

/**
 * Service for interacting with The Movie Database (TMDb) API.
 * Handles searching for movies, fetching images, and retrieving similar movies.
 */
@Service
public class TMDbService {

    /** TMDb API key loaded from application properties. */
    @Value("${tmdb.api.key}")
    private String apiKey;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TMDbService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Searches for movies by title on TMDb.
     *
     * @param title The movie title to search for
     * @return CompletableFuture with TMDbSearchResponse
     */
    public CompletableFuture<TMDbSearchResponse> searchMovie(String title) {
        String url = String.format("https://api.themoviedb.org/3/search/movie?api_key=%s&query=%s",
                apiKey, title.replace(" ", "%20"));
        HttpRequest request = buildHttpRequest(url);

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.body(), TMDbSearchResponse.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to parse TMDb search response", e);
                    }
                });
    }

    /**
     * Gets movie poster and backdrop images from TMDb.
     *
     * @param movieId The TMDb movie ID
     * @return CompletableFuture with TMDbImagesResponse
     */
    public CompletableFuture<TMDbImagesResponse> getMovieImages(Integer movieId) {
        String url = String.format("https://api.themoviedb.org/3/movie/%d/images?api_key=%s",
                movieId, apiKey);
        HttpRequest request = buildHttpRequest(url);

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.body(), TMDbImagesResponse.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to parse TMDb images response", e);
                    }
                });
    }

    /**
     * Gets movies similar to the given movie ID from TMDb.
     *
     * @param movieId The TMDb movie ID
     * @return CompletableFuture with TMDbSimilarResponse
     */
    public CompletableFuture<TMDbSimilarResponse> getSimilarMovies(Integer movieId) {
        String url = String.format("https://api.themoviedb.org/3/movie/%d/similar?api_key=%s",
                movieId, apiKey);
        HttpRequest request = buildHttpRequest(url);

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.body(), TMDbSimilarResponse.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to parse TMDb similar movies response", e);
                    }
                });
    }

    /** Builds an HTTP GET request for the given URL. */
    private HttpRequest buildHttpRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
    }
}