package com.example.moviewatchlist;

import com.example.moviewatchlist.dto.TMDbSearchResponse;
import com.example.moviewatchlist.dto.TMDbImagesResponse;
import com.example.moviewatchlist.dto.TMDbSimilarResponse;
import com.example.moviewatchlist.service.TMDbService;
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

    @Test
    void testSearchMovie_Success() throws Exception {
        // Given
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

        // When
        CompletableFuture<TMDbSearchResponse> future = tmdbService.searchMovie(movieTitle);
        TMDbSearchResponse result = future.join();

        // Then
        assertNotNull(result);
        assertNotNull(result.getResults());
        assertEquals(1, result.getResults().size());

        TMDbSearchResponse.TMDbMovie movie = result.getResults().get(0);
        assertEquals(27205, movie.getId());
        assertEquals("Inception", movie.getTitle());
        assertEquals("2010-07-16", movie.getReleaseDate());
        assertEquals(8.367, movie.getVoteAverage());
    }

    @Test
    void testSearchMovie_NoResults() throws Exception {
        // Given
        String movieTitle = "NonexistentMovie";
        String jsonResponse = """
            {
                "results": []
            }
            """;

        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(CompletableFuture.<HttpResponse<String>>completedFuture(mockResponse));

        // When
        CompletableFuture<TMDbSearchResponse> future = tmdbService.searchMovie(movieTitle);
        TMDbSearchResponse result = future.join();

        // Then
        assertNotNull(result);
        assertNotNull(result.getResults());
        assertTrue(result.getResults().isEmpty());
    }

    @Test
    void testGetMovieImages_Success() throws Exception {
        // Given
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

        // When
        CompletableFuture<TMDbImagesResponse> future = tmdbService.getMovieImages(movieId);
        TMDbImagesResponse result = future.join();

        // Then
        assertNotNull(result);
        assertNotNull(result.getBackdrops());
        assertNotNull(result.getPosters());
        assertEquals(1, result.getBackdrops().size());
        assertEquals(2, result.getPosters().size());
        assertEquals("/poster1.jpg", result.getPosters().get(0).getFile_path());
    }

    @Test
    void testGetSimilarMovies_Success() throws Exception {
        // Given
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

        // When
        CompletableFuture<TMDbSimilarResponse> future = tmdbService.getSimilarMovies(movieId);
        TMDbSimilarResponse result = future.join();

        // Then
        assertNotNull(result);
        assertNotNull(result.getResults());
        assertEquals(2, result.getResults().size());
        assertEquals("The Prestige", result.getResults().get(0).getTitle());
        assertEquals("Shutter Island", result.getResults().get(1).getTitle());
    }

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
}