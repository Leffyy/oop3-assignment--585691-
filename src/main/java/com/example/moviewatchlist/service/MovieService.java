package com.example.moviewatchlist.service;

import com.example.moviewatchlist.dto.*;
import com.example.moviewatchlist.model.Movie;
import com.example.moviewatchlist.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service class for managing movie watchlist operations.
 * Handles movie data fetching from external APIs, persistence, and business logic.
 */
@Service
public class MovieService {
    
    @Autowired
    private MovieRepository movieRepository;
    
    @Autowired
    private OMDbService omdbService;
    
    @Autowired
    private TMDbService tmdbService;
    
    @Autowired
    private ImageDownloadService imageDownloadService;
    
    /**
     * Adds a new movie to the watchlist by searching for it using external APIs.
     * 
     * @param title The movie title to search for
     * @return CompletableFuture containing the saved movie
     * @throws RuntimeException if movie not found or already exists
     */
    public CompletableFuture<Movie> addMovieToWatchlist(String title) {
        return omdbService.getMovieData(title)
                .thenCompose(omdbResponse -> {
                    validateOmdbResponse(omdbResponse);
                    checkIfMovieExists(omdbResponse.getTitle(), omdbResponse.getYear());
                    
                    Movie movie = createMovieFromOmdbData(omdbResponse);
                    return enrichMovieWithTmdbData(movie, title);
                });
    }
    
    /**
     * Validates the OMDb API response.
     * 
     * @param response The OMDb API response
     * @throws RuntimeException if movie not found
     */
    private void validateOmdbResponse(OMDbResponse response) {
        if (!"True".equals(response.getResponse())) {
            throw new RuntimeException("Movie not found: " + response.getError());
        }
    }
    
    /**
     * Checks if a movie already exists in the watchlist.
     * 
     * @param title The movie title
     * @param year The release year
     * @throws RuntimeException if movie already exists
     */
    private void checkIfMovieExists(String title, String year) {
        if (movieRepository.existsByTitleAndReleaseYear(title, year)) {
            throw new RuntimeException("Movie already exists in watchlist");
        }
    }
    
    /**
     * Creates a Movie entity from OMDb API response data.
     * 
     * @param omdbResponse The OMDb API response
     * @return Movie entity with basic information
     */
    private Movie createMovieFromOmdbData(OMDbResponse omdbResponse) {
        Movie movie = new Movie();
        movie.setTitle(omdbResponse.getTitle());
        movie.setReleaseYear(omdbResponse.getYear());
        movie.setDirector(omdbResponse.getDirector());
        movie.setGenre(omdbResponse.getGenre());
        movie.setPlot(omdbResponse.getPlot());
        movie.setRuntime(omdbResponse.getRuntime());
        movie.setImdbRating(omdbResponse.getImdbRating());
        return movie;
    }
    
    /**
     * Enriches movie data with additional information from TMDb API.
     * 
     * @param movie The movie entity to enrich
     * @param title The movie title for searching
     * @return CompletableFuture containing the enriched and saved movie
     */
    private CompletableFuture<Movie> enrichMovieWithTmdbData(Movie movie, String title) {
        return tmdbService.searchMovie(title)
                .thenCompose(tmdbSearchResponse -> {
                    if (tmdbSearchResponse.getResults().isEmpty()) {
                        return CompletableFuture.completedFuture(movieRepository.save(movie));
                    }
                    
                    TMDbSearchResponse.TMDbMovie tmdbMovie = tmdbSearchResponse.getResults().get(0);
                    updateMovieWithTmdbInfo(movie, tmdbMovie);
                    
                    return fetchAdditionalTmdbData(movie, tmdbMovie.getId());
                });
    }
    
    /**
     * Updates movie entity with TMDb search result information.
     * 
     * @param movie The movie entity to update
     * @param tmdbMovie The TMDb search result
     */
    private void updateMovieWithTmdbInfo(Movie movie, TMDbSearchResponse.TMDbMovie tmdbMovie) {
        movie.setTmdbId(tmdbMovie.getId());
        movie.setOverview(tmdbMovie.getOverview());
        movie.setReleaseDate(tmdbMovie.getReleaseDate());
        movie.setVoteAverage(tmdbMovie.getVoteAverage());
    }
    
    /**
     * Fetches additional data (images and similar movies) from TMDb.
     * 
     * @param movie The movie entity
     * @param tmdbId The TMDb movie ID
     * @return CompletableFuture containing the movie with additional data
     */
    private CompletableFuture<Movie> fetchAdditionalTmdbData(Movie movie, Integer tmdbId) {
        CompletableFuture<TMDbImagesResponse> imagesFuture = tmdbService.getMovieImages(tmdbId);
        CompletableFuture<TMDbSimilarResponse> similarFuture = tmdbService.getSimilarMovies(tmdbId);
        
        return CompletableFuture.allOf(imagesFuture, similarFuture)
                .thenCompose(v -> processAdditionalData(movie, imagesFuture.join(), similarFuture.join()));
    }
    
    /**
     * Processes and saves additional TMDb data.
     * 
     * @param movie The movie entity
     * @param imagesResponse TMDb images response
     * @param similarResponse TMDb similar movies response
     * @return CompletableFuture containing the saved movie
     */
    private CompletableFuture<Movie> processAdditionalData(Movie movie, 
                                                          TMDbImagesResponse imagesResponse, 
                                                          TMDbSimilarResponse similarResponse) {
        movie.setSimilarMovies(extractSimilarMovieTitles(similarResponse));
        
        List<String> imagePaths = extractImagePaths(imagesResponse);
        
        if (!imagePaths.isEmpty()) {
            return downloadAndSaveImages(movie, imagePaths);
        } else {
            return CompletableFuture.completedFuture(movieRepository.save(movie));
        }
    }
    
    /**
     * Extracts similar movie titles from TMDb response.
     * 
     * @param similarResponse TMDb similar movies response
     * @return List of similar movie titles (max 10)
     */
    private List<String> extractSimilarMovieTitles(TMDbSimilarResponse similarResponse) {
        if (similarResponse.getResults() == null) {
            return new ArrayList<>();
        }
        
        return similarResponse.getResults().stream()
                .map(TMDbSimilarResponse.SimilarMovie::getTitle)
                .limit(10)
                .toList();
    }
    
    /**
     * Extracts image paths from TMDb images response.
     * 
     * @param imagesResponse TMDb images response
     * @return List of image paths (2 posters + 1 backdrop)
     */
    private List<String> extractImagePaths(TMDbImagesResponse imagesResponse) {
        List<String> imagePaths = new ArrayList<>();
        
        // Add up to 2 poster paths
        if (imagesResponse.getPosters() != null && !imagesResponse.getPosters().isEmpty()) {
            imagesResponse.getPosters().stream()
                    .map(TMDbImagesResponse.ImageData::getFile_path)
                    .limit(2)
                    .forEach(imagePaths::add);
        }
        
        // Add up to 1 backdrop path
        if (imagesResponse.getBackdrops() != null && !imagesResponse.getBackdrops().isEmpty()) {
            imagesResponse.getBackdrops().stream()
                    .map(TMDbImagesResponse.ImageData::getFile_path)
                    .limit(1)
                    .forEach(imagePaths::add);
        }
        
        return imagePaths;
    }
    
    /**
     * Downloads images and saves the movie with image paths.
     * 
     * @param movie The movie entity
     * @param imagePaths List of image paths to download
     * @return CompletableFuture containing the saved movie
     */
    private CompletableFuture<Movie> downloadAndSaveImages(Movie movie, List<String> imagePaths) {
        return imageDownloadService.downloadImages(imagePaths, movie.getTitle())
                .thenApply(downloadedPaths -> {
                    movie.setImagePaths(downloadedPaths);
                    return movieRepository.save(movie);
                });
    }
    
    /**
     * Retrieves a paginated list of movies from the watchlist.
     * 
     * @param page Page number (0-based)
     * @param size Number of items per page
     * @return PaginatedResponse containing movie data
     */
    public PaginatedResponse<MovieResponse> getMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> moviePage = movieRepository.findAll(pageable);
        
        List<MovieResponse> movieResponses = moviePage.getContent().stream()
                .map(MovieResponse::new)
                .toList();
        
        return new PaginatedResponse<>(
                movieResponses,
                moviePage.getNumber(),
                moviePage.getSize(),
                moviePage.getTotalElements(),
                moviePage.getTotalPages()
        );
    }
    
    /**
     * Updates the watched status of a movie.
     * 
     * @param movieId The movie ID
     * @param watched The new watched status
     * @return Optional containing the updated movie, or empty if not found
     */
    public Optional<Movie> updateWatchedStatus(Long movieId, boolean watched) {
        Optional<Movie> movieOpt = movieRepository.findById(movieId);
        if (movieOpt.isPresent()) {
            Movie movie = movieOpt.get();
            movie.setWatched(watched);
            return Optional.of(movieRepository.save(movie));
        }
        return Optional.empty();
    }
    
    /**
     * Updates the rating of a movie.
     * 
     * @param movieId The movie ID
     * @param rating The new rating (1-5 stars, or null to remove)
     * @return Optional containing the updated movie, or empty if not found
     * @throws IllegalArgumentException if rating is not between 1 and 5
     */
    public Optional<Movie> updateRating(Long movieId, Integer rating) {
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        Optional<Movie> movieOpt = movieRepository.findById(movieId);
        if (movieOpt.isPresent()) {
            Movie movie = movieOpt.get();
            movie.setRating(rating);
            return Optional.of(movieRepository.save(movie));
        }
        return Optional.empty();
    }
    
    /**
     * Deletes a movie from the watchlist.
     * 
     * @param movieId The movie ID to delete
     * @return true if movie was deleted, false if not found
     */
    public boolean deleteMovie(Long movieId) {
        if (movieRepository.existsById(movieId)) {
            movieRepository.deleteById(movieId);
            return true;
        }
        return false;
    }
    
    /**
     * Retrieves a specific movie by ID.
     * 
     * @param movieId The movie ID
     * @return Optional containing the movie response, or empty if not found
     */
    public Optional<MovieResponse> getMovieById(Long movieId) {
        return movieRepository.findById(movieId)
                .map(MovieResponse::new);
    }
}