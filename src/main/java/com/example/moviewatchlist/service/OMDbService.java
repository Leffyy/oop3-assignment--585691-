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

/**
 * Service for fetching movie data from the OMDb API.
 * Handles HTTP requests and JSON parsing for OMDb responses.
 */
@Service
public class OMDbService {

    /** OMDb API key loaded from application properties. */
    @Value("${omdb.api.key}")
    private String apiKey;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OMDbService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetches movie data from OMDb API asynchronously.
     *
     * @param title The movie title to search for
     * @return CompletableFuture with OMDbResponse data
     * @throws RuntimeException if the response cannot be parsed
     */
    public CompletableFuture<OMDbResponse> getMovieData(String title) {
        if (title == null || title.trim().isEmpty()) {
            CompletableFuture<OMDbResponse> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalArgumentException("Title cannot be null or blank"));
            return failed;
        }

        String url = buildOmdbUrl(title);
        HttpRequest request = buildHttpRequest(url);

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        System.out.println("OMDb Response: " + response.body());
                        return objectMapper.readValue(response.body(), OMDbResponse.class);
                    } catch (IOException e) {
                        System.out.println("Parsing error: " + e.getMessage());
                        System.out.println("Response body: " + response.body());
                        throw new RuntimeException("Failed to parse OMDb response: " + e.getMessage(), e);
                    }
                });
    }

    /** Builds the OMDb API URL for the given title. */
    private String buildOmdbUrl(String title) {
        return String.format("https://www.omdbapi.com/?t=%s&apikey=%s",
                title.replace(" ", "+"), apiKey);
    }

    /** Builds an HTTP GET request for the given URL. */
    private HttpRequest buildHttpRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
    }
}