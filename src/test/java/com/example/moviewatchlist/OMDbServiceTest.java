package com.example.moviewatchlist;

import com.example.moviewatchlist.dto.OMDbResponse;
import com.example.moviewatchlist.service.OMDbService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OMDbService.
 * Uses Mockito to mock HTTP client behavior and verify OMDb API integration logic.
 */
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class OMDbServiceTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockResponse;

    private OMDbService omdbService;

    @BeforeEach
    void setUp() {
        omdbService = new OMDbService();
        // Use reflection to inject the mock HttpClient and API key
        ReflectionTestUtils.setField(omdbService, "httpClient", mockHttpClient);
        ReflectionTestUtils.setField(omdbService, "apiKey", "test-api-key");
    }

    /**
     * Tests successful retrieval and parsing of movie data from OMDb.
     */
    @Test
    void testGetMovieData_Success() throws Exception {
        String movieTitle = "Inception";
        String jsonResponse = """
            {
                "Title": "Inception",
                "Year": "2010",
                "Director": "Christopher Nolan",
                "Genre": "Action, Sci-Fi, Thriller",
                "Plot": "A thief who steals corporate secrets",
                "Runtime": "148 min",
                "imdbRating": "8.8",
                "Response": "True"
            }
            """;

        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.sendAsync(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
            ))
            .thenReturn(CompletableFuture.<HttpResponse<String>>completedFuture(mockResponse));

        CompletableFuture<OMDbResponse> future = omdbService.getMovieData(movieTitle);
        OMDbResponse result = future.join();

        assertNotNull(result);
        assertEquals("Inception", result.getTitle());
        assertEquals("2010", result.getYear());
        assertEquals("Christopher Nolan", result.getDirector());
        assertEquals("8.8", result.getImdbRating());
        assertEquals("True", result.getResponse());
    }

    /**
     * Tests OMDbService behavior when the movie is not found.
     */
    @Test
    void testGetMovieData_MovieNotFound() throws Exception {
        String movieTitle = "NonexistentMovie";
        String jsonResponse = """
            {
                "Response": "False",
                "Error": "Movie not found!"
            }
            """;

        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.sendAsync(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
            ))
            .thenReturn(CompletableFuture.<HttpResponse<String>>completedFuture(mockResponse));

        CompletableFuture<OMDbResponse> future = omdbService.getMovieData(movieTitle);
        OMDbResponse result = future.join();

        assertNotNull(result);
        assertEquals("False", result.getResponse());
        assertEquals("Movie not found!", result.getError());
        assertNull(result.getTitle());
    }

    /**
     * Tests OMDbService behavior when the response JSON is invalid.
     */
    @Test
    void testGetMovieData_InvalidJson() throws Exception {
        String movieTitle = "Test Movie";
        String invalidJson = "{ invalid json }";

        when(mockResponse.body()).thenReturn(invalidJson);
        when(mockHttpClient.sendAsync(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
            ))
            .thenReturn(CompletableFuture.<HttpResponse<String>>completedFuture(mockResponse));

        CompletableFuture<OMDbResponse> future = omdbService.getMovieData(movieTitle);

        assertThrows(RuntimeException.class, () -> future.join());
    }

    /**
     * Tests that spaces in movie titles are correctly encoded as '+' in the OMDb API URL.
     */
    @Test
    void testGetMovieData_UrlEncoding() throws Exception {
        String movieTitle = "The Lord of the Rings";
        String jsonResponse = """
            {
                "Title": "The Lord of the Rings",
                "Response": "True"
            }
            """;

        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.sendAsync(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
            ))
            .thenAnswer(invocation -> {
                HttpRequest request = invocation.getArgument(0);
                String url = request.uri().toString();
                // Verify that spaces are replaced with +
                assertTrue(url.contains("The+Lord+of+the+Rings"));
                return CompletableFuture.completedFuture(mockResponse);
            });

        omdbService.getMovieData(movieTitle).join();

        verify(mockHttpClient).sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }
}