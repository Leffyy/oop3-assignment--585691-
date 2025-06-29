package com.example.moviewatchlist;

import com.example.moviewatchlist.dto.OMDbResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        // Use reflection to inject the mock HttpClient
        ReflectionTestUtils.setField(omdbService, "httpClient", mockHttpClient);
        ReflectionTestUtils.setField(omdbService, "apiKey", "test-api-key");
    }

    @Test
    void testGetMovieData_Success() throws Exception {
        // Given
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
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // When
        CompletableFuture<OMDbResponse> future = omdbService.getMovieData(movieTitle);
        OMDbResponse result = future.join();

        // Then
        assertNotNull(result);
        assertEquals("Inception", result.getTitle());
        assertEquals("2010", result.getYear());
        assertEquals("Christopher Nolan", result.getDirector());
        assertEquals("8.8", result.getImdbRating());
        assertEquals("True", result.getResponse());
    }

    @Test
    void testGetMovieData_MovieNotFound() throws Exception {
        // Given
        String movieTitle = "NonexistentMovie";
        String jsonResponse = """
            {
                "Response": "False",
                "Error": "Movie not found!"
            }
            """;

        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // When
        CompletableFuture<OMDbResponse> future = omdbService.getMovieData(movieTitle);
        OMDbResponse result = future.join();

        // Then
        assertNotNull(result);
        assertEquals("False", result.getResponse());
        assertEquals("Movie not found!", result.getError());
        assertNull(result.getTitle());
    }

    @Test
    void testGetMovieData_InvalidJson() throws Exception {
        // Given
        String movieTitle = "Test Movie";
        String invalidJson = "{ invalid json }";

        when(mockResponse.body()).thenReturn(invalidJson);
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // When & Then
        CompletableFuture<OMDbResponse> future = omdbService.getMovieData(movieTitle);
        
        assertThrows(RuntimeException.class, () -> future.join());
    }

    @Test
    void testGetMovieData_UrlEncoding() throws Exception {
        // Given
        String movieTitle = "The Lord of the Rings";
        String jsonResponse = """
            {
                "Title": "The Lord of the Rings",
                "Response": "True"
            }
            """;

        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenAnswer(invocation -> {
                HttpRequest request = invocation.getArgument(0);
                String url = request.uri().toString();
                // Verify that spaces are replaced with +
                assertTrue(url.contains("The+Lord+of+the+Rings"));
                return CompletableFuture.completedFuture(mockResponse);
            });

        // When
        omdbService.getMovieData(movieTitle).join();

        // Then
        verify(mockHttpClient).sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }
}