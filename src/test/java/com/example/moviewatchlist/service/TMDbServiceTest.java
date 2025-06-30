package com.example.moviewatchlist.service;

import com.example.moviewatchlist.dto.TMDbSearchResponse;
import com.example.moviewatchlist.dto.TMDbImagesResponse;
import com.example.moviewatchlist.dto.TMDbSimilarResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Unit tests for TMDbService.
 * Uses Mockito to mock HTTP client behavior and verify TMDb API integration logic.
 */
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class TMDbServiceTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockResponse;

    private TMDbService tmdbService;

    @BeforeEach
    void setUp() {
        tmdbService = new TMDbService();
        ReflectionTestUtils.setField(tmdbService, "httpClient", mockHttpClient);
        ReflectionTestUtils.setField(tmdbService, "apiKey", "test-tmdb-key");
    }

    /**
     * Tests successful search for a movie using TMDb API.
     */
    @Test
    void testSearchMovie_Success() throws Exception {
        String movieTitle = "Inception";
        String jsonResponse = """
            {
                "results": [
                    {
                        "id": 27205,
                        "title": "Inception",
                        "overview": "Cobb steals secrets from subconscious",
                        "release_date": "2010-07-16",
                        "vote_average": 8.367,
                        "poster_path": "/path/to/poster.jpg"
                    }
                ]
            }
            """;

        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(CompletableFuture.<HttpResponse<String>>completedFuture(mockResponse));

        CompletableFuture<TMDbSearchResponse> future = tmdbService.searchMovie(movieTitle);
        TMDbSearchResponse result = future.join();

        assertNotNull(result);
        assertNotNull(result.getResults());
        assertEquals(1, result.getResults().size());

        TMDbSearchResponse.TMDbMovie movie = result.getResults().get(0);
        assertEquals(27205, movie.getId());
        assertEquals("Inception", movie.getTitle());
        assertEquals("2010-07-16", movie.getReleaseDate());
        assertEquals(8.367, movie.getVoteAverage());
    }

    /**
     * Tests TMDbService searchMovie when no results are found.
     */
    @Test
    void testSearchMovie_NoResults() throws Exception {
        String movieTitle = "NonexistentMovie";
        String jsonResponse = """
            {
                "results": []
            }
            """;

        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(CompletableFuture.<HttpResponse<String>>completedFuture(mockResponse));

        CompletableFuture<TMDbSearchResponse> future = tmdbService.searchMovie(movieTitle);
        TMDbSearchResponse result = future.join();

        assertNotNull(result);
        assertNotNull(result.getResults());
        assertTrue(result.getResults().isEmpty());
    }

    /**
     * Tests successful retrieval of movie images from TMDb API.
     */
    @Test
    void testGetMovieImages_Success() throws Exception {
        Integer movieId = 27205;
        String jsonResponse = """
            {
                "backdrops": [
                    {
                        "file_path": "/backdrop1.jpg",
                        "vote_average": 8.5
                    }
                ],
                "posters": [
                    {
                        "file_path": "/poster1.jpg",
                        "vote_average": 9.0
                    },
                    {
                        "file_path": "/poster2.jpg",
                        "vote_average": 8.8
                    }
                ]
            }
            """;

        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(CompletableFuture.<HttpResponse<String>>completedFuture(mockResponse));

        CompletableFuture<TMDbImagesResponse> future = tmdbService.getMovieImages(movieId);
        TMDbImagesResponse result = future.join();

        assertNotNull(result);
        assertNotNull(result.getBackdrops());
        assertNotNull(result.getPosters());
        assertEquals(1, result.getBackdrops().size());
        assertEquals(2, result.getPosters().size());
        assertEquals("/poster1.jpg", result.getPosters().get(0).getFile_path());
    }

    /**
     * Tests successful retrieval of similar movies from TMDb API.
     */
    @Test
    void testGetSimilarMovies_Success() throws Exception {
        Integer movieId = 27205;
        String jsonResponse = """
            {
                "results": [
                    {
                        "id": 1124,
                        "title": "The Prestige"
                    },
                    {
                        "id": 11324,
                        "title": "Shutter Island"
                    }
                ]
            }
            """;

        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(CompletableFuture.<HttpResponse<String>>completedFuture(mockResponse));

        CompletableFuture<TMDbSimilarResponse> future = tmdbService.getSimilarMovies(movieId);
        TMDbSimilarResponse result = future.join();

        assertNotNull(result);
        assertNotNull(result.getResults());
        assertEquals(2, result.getResults().size());
        assertEquals("The Prestige", result.getResults().get(0).getTitle());
        assertEquals("Shutter Island", result.getResults().get(1).getTitle());
    }

    /**
     * Tests URL encoding of movie title in searchMovie method.
     */
    @Test
    void testSearchMovie_UrlEncoding() throws Exception {
        // Given
        String movieTitle = "The Dark Knight";
        String jsonResponse = """
            {
                "results": []
            }
            """;

        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenAnswer(invocation -> {
                HttpRequest request = invocation.getArgument(0);
                String url = request.uri().toString();
                // Verify URL encoding
                assertTrue(url.contains("The%20Dark%20Knight"));
                return CompletableFuture.<HttpResponse<String>>completedFuture(mockResponse);
            });

        // When
        tmdbService.searchMovie(movieTitle).join();

        // Then
        verify(mockHttpClient).sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    /**
     * Tests TMDbService.searchMovie behavior when TMDb API returns invalid JSON.
     */
    @Test
    void testSearchMovie_InvalidJson() throws Exception {
        String movieTitle = "Inception";
        String invalidJson = "{ invalid json }";

        when(mockResponse.body()).thenReturn(invalidJson);
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));

        CompletableFuture<TMDbSearchResponse> future = tmdbService.searchMovie(movieTitle);

        assertThrows(RuntimeException.class, () -> future.join());
    }

    /**
     * Tests TMDbService.searchMovie behavior when HttpClient throws an exception.
     */
    @Test
    void testSearchMovie_HttpClientThrowsException() throws Exception {
        String movieTitle = "Inception";
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Network error")));

        CompletableFuture<TMDbSearchResponse> future = tmdbService.searchMovie(movieTitle);

        assertThrows(RuntimeException.class, () -> future.join());
    }

    static class FailingObjectMapper extends ObjectMapper {
        @Override
        public <T> T readValue(String content, Class<T> valueType) {
            throw new RuntimeException("Parse error");
        }
    }

    @Test
    void getMovieImages_throwsRuntimeExceptionOnParseError() throws Exception {
        TMDbService service = new TMDbService();
        ObjectMapper failingMapper = new FailingObjectMapper();
        ReflectionTestUtils.setField(service, "objectMapper", failingMapper);

        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn("{}");
        when(mockClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));
        ReflectionTestUtils.setField(service, "httpClient", mockClient);

        CompletableFuture<TMDbImagesResponse> future = service.getMovieImages(123);
        RuntimeException ex = assertThrows(RuntimeException.class, future::join);
        Throwable real = ex.getCause();
        assertEquals("Failed to parse TMDb images response", real.getMessage());
        assertNotNull(real.getCause());
        assertEquals("Parse error", real.getCause().getMessage());
    }

    @Test
    void getSimilarMovies_throwsRuntimeExceptionOnParseError() throws Exception {
        TMDbService service = new TMDbService();
        ObjectMapper failingMapper = new ObjectMapper() {
            @Override
            public <T> T readValue(String content, Class<T> valueType) {
                throw new RuntimeException("Parse error");
            }
        };
        ReflectionTestUtils.setField(service, "objectMapper", failingMapper);

        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn("{}");
        when(mockClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));
        ReflectionTestUtils.setField(service, "httpClient", mockClient);

        CompletableFuture<TMDbSimilarResponse> future = service.getSimilarMovies(123);
        RuntimeException ex = assertThrows(RuntimeException.class, future::join);
        Throwable real = ex.getCause();
        assertEquals("Failed to parse TMDb similar movies response", real.getMessage());
        assertNotNull(real.getCause());
        assertEquals("Parse error", real.getCause().getMessage());
    }
}