package com.example.moviewatchlist;

import com.example.moviewatchlist.dto.*;
import com.example.moviewatchlist.model.Movie;
import com.example.moviewatchlist.repository.MovieRepository;
import com.example.moviewatchlist.service.ImageDownloadService;
import com.example.moviewatchlist.service.MovieService;
import com.example.moviewatchlist.service.OMDbService;
import com.example.moviewatchlist.service.TMDbService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private OMDbService omdbService;

    @Mock
    private TMDbService tmdbService;

    @Mock
    private ImageDownloadService imageDownloadService;

    @InjectMocks
    private MovieService movieService;

    private Movie testMovie;
    private OMDbResponse omdbResponse;
    private TMDbSearchResponse tmdbSearchResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
        testMovie = new Movie("Inception", "2010", "Christopher Nolan", "Sci-Fi");
        testMovie.setId(1L);
        testMovie.setPlot("A thief who enters dreams");
        testMovie.setImdbRating("8.8");
        
        // Setup OMDb response
        omdbResponse = new OMDbResponse();
        omdbResponse.setTitle("Inception");
        omdbResponse.setYear("2010");
        omdbResponse.setDirector("Christopher Nolan");
        omdbResponse.setGenre("Sci-Fi");
        omdbResponse.setPlot("A thief who enters dreams");
        omdbResponse.setImdbRating("8.8");
        omdbResponse.setResponse("True");
        
        // Setup TMDb response
        tmdbSearchResponse = new TMDbSearchResponse();
        TMDbSearchResponse.TMDbMovie tmdbMovie = new TMDbSearchResponse.TMDbMovie();
        tmdbMovie.setId(27205);
        tmdbMovie.setTitle("Inception");
        tmdbMovie.setOverview("Cobb steals secrets from subconscious");
        tmdbSearchResponse.setResults(Arrays.asList(tmdbMovie));
    }

    @Test
    void testAddMovieToWatchlist_Success() {
        // Given
        String movieTitle = "Inception";
        when(omdbService.getMovieData(movieTitle))
            .thenReturn(CompletableFuture.completedFuture(omdbResponse));
        when(movieRepository.existsByTitleAndReleaseYear("Inception", "2010"))
            .thenReturn(false);
        when(tmdbService.searchMovie(movieTitle))
            .thenReturn(CompletableFuture.completedFuture(tmdbSearchResponse));
        when(tmdbService.getMovieImages(27205))
            .thenReturn(CompletableFuture.completedFuture(new TMDbImagesResponse()));
        when(tmdbService.getSimilarMovies(27205))
            .thenReturn(CompletableFuture.completedFuture(new TMDbSimilarResponse()));
        when(movieRepository.save(any(Movie.class)))
            .thenReturn(testMovie);

        // When
        CompletableFuture<Movie> result = movieService.addMovieToWatchlist(movieTitle);
        Movie savedMovie = result.join();

        // Then
        assertNotNull(savedMovie);
        assertEquals("Inception", savedMovie.getTitle());
        assertEquals("2010", savedMovie.getReleaseYear());
        assertEquals("Christopher Nolan", savedMovie.getDirector());
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    void testAddMovieToWatchlist_MovieNotFound() {
        // Given
        String movieTitle = "NonExistentMovie";
        OMDbResponse errorResponse = new OMDbResponse();
        errorResponse.setResponse("False");
        errorResponse.setError("Movie not found!");
        
        when(omdbService.getMovieData(movieTitle))
            .thenReturn(CompletableFuture.completedFuture(errorResponse));

        // When & Then
        CompletableFuture<Movie> result = movieService.addMovieToWatchlist(movieTitle);
        
        assertThrows(RuntimeException.class, () -> result.join());
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void testAddMovieToWatchlist_AlreadyExists() {
        // Given
        String movieTitle = "Inception";
        when(omdbService.getMovieData(movieTitle))
            .thenReturn(CompletableFuture.completedFuture(omdbResponse));
        when(movieRepository.existsByTitleAndReleaseYear("Inception", "2010"))
            .thenReturn(true);

        // When & Then
        CompletableFuture<Movie> result = movieService.addMovieToWatchlist(movieTitle);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> result.join());
        assertTrue(exception.getMessage().contains("already exists"));
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void testGetMovies() {
        // Given
        List<Movie> movies = Arrays.asList(testMovie);
        Page<Movie> moviePage = new PageImpl<>(movies, PageRequest.of(0, 10), 1);
        when(movieRepository.findAll(any(Pageable.class))).thenReturn(moviePage);

        // When
        PaginatedResponse<MovieResponse> response = movieService.getMovies(0, 10);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Inception", response.getContent().get(0).getTitle());
        assertEquals(0, response.getPageNumber());
        assertEquals(10, response.getPageSize());
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void testUpdateWatchedStatus_Success() {
        // Given
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);

        // When
        Optional<Movie> result = movieService.updateWatchedStatus(1L, true);

        // Then
        assertTrue(result.isPresent());
        assertTrue(result.get().isWatched());
        verify(movieRepository).save(testMovie);
    }

    @Test
    void testUpdateWatchedStatus_MovieNotFound() {
        // Given
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Movie> result = movieService.updateWatchedStatus(999L, true);

        // Then
        assertFalse(result.isPresent());
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void testUpdateRating_ValidRating() {
        // Given
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
        when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);

        // When
        Optional<Movie> result = movieService.updateRating(1L, 4);

        // Then
        assertTrue(result.isPresent());
        assertEquals(4, result.get().getRating());
        verify(movieRepository).save(testMovie);
    }

    @Test
    void testUpdateRating_InvalidRating() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> movieService.updateRating(1L, 6));
        assertThrows(IllegalArgumentException.class, 
            () -> movieService.updateRating(1L, 0));
    }

    @Test
    void testDeleteMovie_Success() {
        // Given
        when(movieRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = movieService.deleteMovie(1L);

        // Then
        assertTrue(result);
        verify(movieRepository).deleteById(1L);
    }

    @Test
    void testDeleteMovie_NotFound() {
        // Given
        when(movieRepository.existsById(999L)).thenReturn(false);

        // When
        boolean result = movieService.deleteMovie(999L);

        // Then
        assertFalse(result);
        verify(movieRepository, never()).deleteById(anyLong());
    }

    @Test
    void testGetMovieById_Found() {
        // Given
        when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));

        // When
        Optional<MovieResponse> result = movieService.getMovieById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Inception", result.get().getTitle());
    }

    @Test
    void testGetMovieById_NotFound() {
        // Given
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<MovieResponse> result = movieService.getMovieById(999L);

        // Then
        assertFalse(result.isPresent());
    }
}