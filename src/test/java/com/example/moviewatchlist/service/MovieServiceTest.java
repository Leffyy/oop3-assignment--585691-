package com.example.moviewatchlist.service;

import com.example.moviewatchlist.dto.*;
import com.example.moviewatchlist.model.Movie;
import com.example.moviewatchlist.repository.MovieRepository;

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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the MovieService class.
 * Uses Mockito to mock dependencies and verify service logic.
 */
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
        // Setup test data using builder
        testMovie = Movie.builder()
            .title("Inception")
            .releaseYear("2010")
            .director("Christopher Nolan")
            .genre("Sci-Fi")
            .id(1L)
            .build();
        testMovie.setPlot("A thief who enters dreams");
        testMovie.setImdbRating("8.8");
        testMovie.setWatched(false); // <-- Add this
        testMovie.setRating(3);      // <-- And this
        
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

    /**
     * Tests adding a movie to the watchlist when all external calls succeed.
     */
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
        assertTrue(Boolean.TRUE.equals(result.get().getWatched()));
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

    @Test
    void addMovieByTitle_throwsExceptionIfTitleIsNull() {
        CompletionException ex = assertThrows(CompletionException.class, () -> movieService.addMovieByTitle(null).join());
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void addMovieByTitle_throwsExceptionIfTitleIsBlank() {
        CompletionException ex = assertThrows(CompletionException.class, () -> movieService.addMovieByTitle("   ").join());
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void addMovieToWatchlist_throwsExceptionIfTitleIsNull() {
        CompletionException ex = assertThrows(CompletionException.class, () -> movieService.addMovieToWatchlist(null).join());
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void addMovieToWatchlist_throwsExceptionIfTitleIsBlank() {
        CompletionException ex = assertThrows(CompletionException.class, () -> movieService.addMovieToWatchlist("   ").join());
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void addMovieToWatchlist_throwsExceptionIfOmdbFutureIsNull() {
        when(omdbService.getMovieData(anyString())).thenReturn(null);
        CompletionException ex = assertThrows(CompletionException.class, () -> movieService.addMovieToWatchlist("Some Movie").join());
        assertTrue(ex.getCause() instanceof NullPointerException);
    }

    @Test
    void searchMovies_returnsEmptyListForNullOrShortQuery() {
        MovieService service = new MovieService();
        // You may need to inject mocks for tmdbService if used elsewhere

        CompletableFuture<List<Map<String, Object>>> result1 = service.searchMovies(null);
        CompletableFuture<List<Map<String, Object>>> result2 = service.searchMovies(" ");
        CompletableFuture<List<Map<String, Object>>> result3 = service.searchMovies("a");

        assertTrue(result1.join().isEmpty());
        assertTrue(result2.join().isEmpty());
        assertTrue(result3.join().isEmpty());
    }

    @Test
    void searchMovies_returnsEmptyListIfTmdbResultsNull() {
        TMDbService tmdbService = mock(TMDbService.class);
        MovieService service = new MovieService();
        ReflectionTestUtils.setField(service, "tmdbService", tmdbService);

        TMDbSearchResponse response = new TMDbSearchResponse();
        response.setResults(null);

        when(tmdbService.searchMovie(anyString()))
            .thenReturn(CompletableFuture.completedFuture(response));

        CompletableFuture<List<Map<String, Object>>> result = service.searchMovies("test");
        assertTrue(result.join().isEmpty());
    }

    @Test
    void searchMovies_returnsMappedResults() {
        TMDbService tmdbService = mock(TMDbService.class);
        MovieService service = new MovieService();
        ReflectionTestUtils.setField(service, "tmdbService", tmdbService);

        TMDbSearchResponse.TMDbMovie movie = new TMDbSearchResponse.TMDbMovie();
        movie.setId(1);
        movie.setTitle("Test Movie");
        movie.setReleaseDate("2020-01-01");
        movie.setVoteAverage(8.5);
        movie.setPosterPath("/poster.jpg");

        TMDbSearchResponse response = new TMDbSearchResponse();
        response.setResults(List.of(movie));

        when(tmdbService.searchMovie(anyString()))
            .thenReturn(CompletableFuture.completedFuture(response));

        CompletableFuture<List<Map<String, Object>>> result = service.searchMovies("test");
        List<Map<String, Object>> list = result.join();
        assertEquals(1, list.size());
        assertEquals("Test Movie", list.get(0).get("title"));
    }

    @Test
    void searchMovies_mapsAllFieldsInConvertToSearchResult() {
        TMDbSearchResponse.TMDbMovie movie = new TMDbSearchResponse.TMDbMovie();
        movie.setId(42);
        movie.setTitle("My Movie");
        movie.setReleaseDate("2024-01-01");
        movie.setVoteAverage(7.8);
        movie.setPosterPath("/poster.png");

        TMDbSearchResponse response = new TMDbSearchResponse();
        response.setResults(List.of(movie));

        when(tmdbService.searchMovie(anyString()))
            .thenReturn(CompletableFuture.completedFuture(response));

        List<Map<String, Object>> results = movieService.searchMovies("My Movie").join();
        assertEquals(1, results.size());
        Map<String, Object> result = results.get(0);
        assertEquals(42, result.get("id"));
        assertEquals("My Movie", result.get("title"));
        assertEquals("2024-01-01", result.get("releaseDate"));
        assertEquals(7.8, result.get("voteAverage"));
        assertEquals("/poster.png", result.get("posterPath"));
    }

    @Test
    void enrichMovieWithTmdbData_throwsExceptionIfTmdbFutureIsNull() {
        Movie movie = new Movie();
        String title = "Some Movie";
        when(tmdbService.searchMovie(anyString())).thenReturn(null);

        CompletableFuture<Movie> result = ReflectionTestUtils.invokeMethod(
            movieService, "enrichMovieWithTmdbData", movie, title);

        assertNotNull(result, "ReflectionTestUtils.invokeMethod returned null");
        CompletionException ex = assertThrows(CompletionException.class, result::join);
        assertTrue(ex.getCause() instanceof NullPointerException);
        assertEquals("TMDbService.searchMovie returned null", ex.getCause().getMessage());
    }

    @Test
    void enrichMovieWithTmdbData_savesMovieIfTmdbResultsEmpty() {
        Movie movie = new Movie();
        String title = "Some Movie";

        // TMDb returns empty results
        TMDbSearchResponse tmdbResponse = new TMDbSearchResponse();
        tmdbResponse.setResults(new java.util.ArrayList<>());

        CompletableFuture<TMDbSearchResponse> tmdbFuture = CompletableFuture.completedFuture(tmdbResponse);
        when(tmdbService.searchMovie(anyString())).thenReturn(tmdbFuture);

        Movie savedMovie = new Movie();
        when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);

        // Use reflection to call the private method
        CompletableFuture<Movie> result = ReflectionTestUtils.invokeMethod(
            movieService, "enrichMovieWithTmdbData", movie, title);

        assertNotNull(result, "ReflectionTestUtils.invokeMethod returned null");
        assertSame(savedMovie, result.join());
        verify(movieRepository).save(movie);
    }

    @Test
    void processAdditionalData_downloadsAndSavesImagesIfImagePathsNotEmpty() {
        Movie movie = new Movie();
        TMDbImagesResponse imagesResponse = mock(TMDbImagesResponse.class);
        TMDbSimilarResponse similarResponse = mock(TMDbSimilarResponse.class);

        // Use ReflectionTestUtils to set up a non-empty imagePaths list
        List<String> imagePaths = List.of("/poster1.jpg", "/backdrop1.jpg");

        // Use a spy to override extractImagePaths
        MovieService spyService = spy(movieService);
        doReturn(imagePaths).when(spyService).extractImagePaths(imagesResponse);

        Movie savedMovie = new Movie();
        when(imageDownloadService.downloadImages(anyList(), any())).thenReturn(CompletableFuture.completedFuture(imagePaths));
        when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);

        CompletableFuture<Movie> result = ReflectionTestUtils.invokeMethod(
            spyService, "processAdditionalData", movie, imagesResponse, similarResponse);

        assertNotNull(result);
        assertSame(savedMovie, result.join());
        verify(imageDownloadService).downloadImages(anyList(), any());
        verify(movieRepository).save(movie);
    }

    @Test
    void extractSimilarMovieTitles_returnsEmptyListIfResultsNull() {
        TMDbSimilarResponse similarResponse = mock(TMDbSimilarResponse.class);
        when(similarResponse.getResults()).thenReturn(null);

        List<String> result = ReflectionTestUtils.invokeMethod(
            movieService, "extractSimilarMovieTitles", similarResponse);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void extractSimilarMovieTitles_returnsTitlesFromResults() {
        TMDbSimilarResponse similarResponse = mock(TMDbSimilarResponse.class);
        TMDbSimilarResponse.SimilarMovie movie1 = mock(TMDbSimilarResponse.SimilarMovie.class);
        TMDbSimilarResponse.SimilarMovie movie2 = mock(TMDbSimilarResponse.SimilarMovie.class);

        when(movie1.getTitle()).thenReturn("Movie 1");
        when(movie2.getTitle()).thenReturn("Movie 2");
        when(similarResponse.getResults()).thenReturn(List.of(movie1, movie2));

        List<String> result = ReflectionTestUtils.invokeMethod(
            movieService, "extractSimilarMovieTitles", similarResponse);

        assertEquals(List.of("Movie 1", "Movie 2"), result);
    }

    @Test
    void addPosterPaths_addsUpToTwoPosterPaths() {
        TMDbImagesResponse imagesResponse = mock(TMDbImagesResponse.class);
        TMDbImagesResponse.ImageData poster1 = mock(TMDbImagesResponse.ImageData.class);
        TMDbImagesResponse.ImageData poster2 = mock(TMDbImagesResponse.ImageData.class);
        TMDbImagesResponse.ImageData poster3 = mock(TMDbImagesResponse.ImageData.class);

        when(poster1.getFile_path()).thenReturn("/poster1.jpg");
        when(poster2.getFile_path()).thenReturn("/poster2.jpg");
        // when(poster3.getFile_path()).thenReturn("/poster3.jpg");
        when(imagesResponse.getPosters()).thenReturn(List.of(poster1, poster2, poster3));

        List<String> imagePaths = new ArrayList<>();
        ReflectionTestUtils.invokeMethod(movieService, "addPosterPaths", imagesResponse, imagePaths);

        assertEquals(List.of("/poster1.jpg", "/poster2.jpg"), imagePaths);
    }

    @Test
    void addPosterPaths_doesNothingIfPostersNullOrEmpty() {
        TMDbImagesResponse imagesResponse = mock(TMDbImagesResponse.class);
        when(imagesResponse.getPosters()).thenReturn(null);

        List<String> imagePaths = new ArrayList<>();
        ReflectionTestUtils.invokeMethod(movieService, "addPosterPaths", imagesResponse, imagePaths);
        assertTrue(imagePaths.isEmpty());

        when(imagesResponse.getPosters()).thenReturn(List.of());
        ReflectionTestUtils.invokeMethod(movieService, "addPosterPaths", imagesResponse, imagePaths);
        assertTrue(imagePaths.isEmpty());
    }

    @Test
    void addBackdropPaths_addsOnlyOneBackdropPath() {
        TMDbImagesResponse imagesResponse = mock(TMDbImagesResponse.class);
        TMDbImagesResponse.ImageData backdrop1 = mock(TMDbImagesResponse.ImageData.class);
        TMDbImagesResponse.ImageData backdrop2 = mock(TMDbImagesResponse.ImageData.class);

        when(backdrop1.getFile_path()).thenReturn("/backdrop1.jpg");
        // when(backdrop2.getFile_path()).thenReturn("/backdrop2.jpg");
        when(imagesResponse.getBackdrops()).thenReturn(List.of(backdrop1, backdrop2));

        List<String> imagePaths = new ArrayList<>();
        ReflectionTestUtils.invokeMethod(movieService, "addBackdropPaths", imagesResponse, imagePaths);

        assertEquals(List.of("/backdrop1.jpg"), imagePaths);
    }

    @Test
    void addBackdropPaths_doesNothingIfBackdropsNullOrEmpty() {
        TMDbImagesResponse imagesResponse = mock(TMDbImagesResponse.class);
        when(imagesResponse.getBackdrops()).thenReturn(null);

        List<String> imagePaths = new ArrayList<>();
        ReflectionTestUtils.invokeMethod(movieService, "addBackdropPaths", imagesResponse, imagePaths);
        assertTrue(imagePaths.isEmpty());

        when(imagesResponse.getBackdrops()).thenReturn(List.of());
        ReflectionTestUtils.invokeMethod(movieService, "addBackdropPaths", imagesResponse, imagePaths);
        assertTrue(imagePaths.isEmpty());
    }

    @Test
    void downloadAndSaveImages_setsImagePathsAndSavesMovie() {
        Movie movie = new Movie();
        movie.setTitle("Test Movie");
        List<String> imagePaths = List.of("/img1.jpg", "/img2.jpg");
        List<String> downloadedPaths = List.of("/local/img1.jpg", "/local/img2.jpg");

        when(imageDownloadService.downloadImages(imagePaths, "Test Movie"))
            .thenReturn(CompletableFuture.completedFuture(downloadedPaths));

        Movie savedMovie = new Movie();
        when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);

        CompletableFuture<Movie> result = ReflectionTestUtils.invokeMethod(
            movieService, "downloadAndSaveImages", movie, imagePaths);

        assertNotNull(result);
        assertSame(savedMovie, result.join());
        assertEquals(downloadedPaths, movie.getImagePaths());
        verify(movieRepository).save(movie);
    }

    @Test
    void getMovies_setsPageToZeroIfNegative() {
        when(movieRepository.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        PaginatedResponse<MovieResponse> response = movieService.getMovies(-5, 10);
        assertEquals(0, response.getPageNumber());
    }

    @Test
    void getMovies_setsSizeToTenIfLessThanOne() {
        List<Movie> movies = List.of(testMovie);
        Page<Movie> moviePage = new PageImpl<>(movies, PageRequest.of(0, 10), 1);
        when(movieRepository.findAll(any(Pageable.class))).thenReturn(moviePage);

        PaginatedResponse<MovieResponse> response = movieService.getMovies(0, 0);
        assertEquals(10, response.getPageSize());
    }

    @Test
    void getMovies_setsSizeToTenIfGreaterThanHundred() {
        List<Movie> movies = List.of(testMovie);
        Page<Movie> moviePage = new PageImpl<>(movies, PageRequest.of(0, 10), 1);
        when(movieRepository.findAll(any(Pageable.class))).thenReturn(moviePage);

        PaginatedResponse<MovieResponse> response = movieService.getMovies(0, 101);
        assertEquals(10, response.getPageSize());
    }

    @Test
    void testUpdateWatchedStatus_NullWatched() {
        // Given
                

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            movieService.updateWatchedStatus(1L, null);
        });
        assertEquals("Watched status is required", exception.getMessage());
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void updateWatchedStatus_throwsExceptionIfWatchedIsNull() {
        assertThrows(IllegalArgumentException.class, () -> movieService.updateWatchedStatus(1L, null));
    }

    @Test
    void updateRating_returnsEmptyIfMovieNotFound() {
        // Given
        when(movieRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Movie> result = movieService.updateRating(999L, 3);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void updateRating_throwsExceptionIfRatingIsNull() {
        assertThrows(IllegalArgumentException.class, () -> movieService.updateRating(1L, null));
    }

    @Test
    void addMovieByTitle_returnsMovieForValidTitle() {
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

        CompletableFuture<Movie> result = movieService.addMovieByTitle(movieTitle);
        Movie savedMovie = result.join();

        assertNotNull(savedMovie);
        assertEquals("Inception", savedMovie.getTitle());
    }
}