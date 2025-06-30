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
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

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
     * Search for movies using TMDb API.
     *
     * @param query Search query (must be at least 2 characters)
     * @return List of movie search results
     * @throws IllegalArgumentException if query is too short
     */
    public CompletableFuture<List<Map<String, Object>>> searchMovies(String query) {
        if (query == null || query.trim().length() < 2) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        return tmdbService.searchMovie(query.trim())
                .thenApply(response -> {
                    if (response.getResults() == null) {
                        return new ArrayList<>();
                    }
                    return response.getResults().stream()
                            .limit(10)
                            .map(this::convertToSearchResult)
                            .collect(Collectors.toList());
                });
    }

    /**
     * Converts TMDb movie to simplified search result.
     *
     * @param movie TMDb movie object
     * @return Map containing simplified movie data
     */
    private Map<String, Object> convertToSearchResult(TMDbSearchResponse.TMDbMovie movie) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", movie.getId());
        result.put("title", movie.getTitle());
        result.put("releaseDate", movie.getReleaseDate());
        result.put("voteAverage", movie.getVoteAverage());
        result.put("posterPath", movie.getPosterPath());
        return result;
    }

    /**
     * Adds a movie to the watchlist by title.
     * Validates the title before processing.
     *
     * @param title Movie title
     * @return CompletableFuture containing the saved movie
     */
    public CompletableFuture<Movie> addMovieByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            CompletableFuture<Movie> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalArgumentException("Movie title is required"));
            return failed;
        }
        return addMovieToWatchlist(title.trim());
    }

    /**
     * Adds a new movie to the watchlist by searching for it using external APIs.
     *
     * @param title The movie title to search for
     * @return CompletableFuture containing the saved movie
     */
    public CompletableFuture<Movie> addMovieToWatchlist(String title) {
        if (isBlank(title)) {
            return failedFuture(new IllegalArgumentException("Title cannot be null or blank"));
        }

        CompletableFuture<OMDbResponse> omdbFuture = omdbService.getMovieData(title);
        if (omdbFuture == null) {
            return failedFuture(new NullPointerException("OMDbService.getMovieData returned null"));
        }

        return omdbFuture.thenCompose(omdbResponse -> handleOmdbResponse(omdbResponse, title));
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private <T> CompletableFuture<T> failedFuture(Throwable ex) {
        CompletableFuture<T> failed = new CompletableFuture<>();
        failed.completeExceptionally(ex);
        return failed;
    }

    // Extracted from addMovieToWatchlist for method size and clarity
    private CompletableFuture<Movie> handleOmdbResponse(OMDbResponse omdbResponse, String title) {
        try {
            validateOmdbResponse(omdbResponse);
            checkIfMovieExists(omdbResponse.getTitle(), omdbResponse.getYear());
            Movie movie = createMovieFromOmdbData(omdbResponse);
            return enrichMovieWithTmdbData(movie, title);
        } catch (Exception ex) {
            CompletableFuture<Movie> failed = new CompletableFuture<>();
            failed.completeExceptionally(ex);
            return failed;
        }
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
        CompletableFuture<TMDbSearchResponse> tmdbFuture = tmdbService.searchMovie(title);
        if (tmdbFuture == null) {
            CompletableFuture<Movie> failed = new CompletableFuture<>();
            failed.completeExceptionally(new NullPointerException("TMDbService.searchMovie returned null"));
            return failed;
        }
        return tmdbFuture.thenCompose(tmdbSearchResponse -> handleTmdbSearchResponse(movie, tmdbSearchResponse));
    }

    // Extracted from enrichMovieWithTmdbData for method size and clarity
    private CompletableFuture<Movie> handleTmdbSearchResponse(Movie movie, TMDbSearchResponse tmdbSearchResponse) {
        if (tmdbSearchResponse.getResults().isEmpty()) {
            return CompletableFuture.completedFuture(movieRepository.save(movie));
        }
        TMDbSearchResponse.TMDbMovie tmdbMovie = tmdbSearchResponse.getResults().get(0);
        updateMovieWithTmdbInfo(movie, tmdbMovie);
        return fetchAdditionalTmdbData(movie, tmdbMovie.getId());
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
    List<String> extractImagePaths(TMDbImagesResponse imagesResponse) {
        List<String> imagePaths = new ArrayList<>();
        addPosterPaths(imagesResponse, imagePaths);
        addBackdropPaths(imagesResponse, imagePaths);
        return imagePaths;
    }

    // Extracted helpers for extractImagePaths to keep methods small
    private void addPosterPaths(TMDbImagesResponse imagesResponse, List<String> imagePaths) {
        if (imagesResponse.getPosters() != null && !imagesResponse.getPosters().isEmpty()) {
            imagesResponse.getPosters().stream()
                    .map(TMDbImagesResponse.ImageData::getFile_path)
                    .limit(2)
                    .forEach(imagePaths::add);
        }
    }

    private void addBackdropPaths(TMDbImagesResponse imagesResponse, List<String> imagePaths) {
        if (imagesResponse.getBackdrops() != null && !imagesResponse.getBackdrops().isEmpty()) {
            imagesResponse.getBackdrops().stream()
                    .map(TMDbImagesResponse.ImageData::getFile_path)
                    .limit(1)
                    .forEach(imagePaths::add);
        }
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
     * Validates pagination parameters.
     *
     * @param page Page number (0-based)
     * @param size Number of items per page
     * @return PaginatedResponse containing movie data
     */
    public PaginatedResponse<MovieResponse> getMovies(int page, int size) {
        Pageable pageable = createPageable(page, size);
        Page<Movie> moviePage = movieRepository.findAll(pageable);
        List<MovieResponse> movieResponses = mapToMovieResponses(moviePage.getContent());

        return new PaginatedResponse<>(
                movieResponses,
                moviePage.getNumber(),
                moviePage.getSize(),
                moviePage.getTotalElements(),
                moviePage.getTotalPages()
        );
    }

    private Pageable createPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = (size < 1 || size > 100) ? 10 : size;
        return PageRequest.of(safePage, safeSize);
    }

    private List<MovieResponse> mapToMovieResponses(List<Movie> movies) {
        return movies.stream().map(MovieResponse::new).toList();
    }

    /**
     * Updates the watched status of a movie.
     *
     * @param movieId The movie ID
     * @param watched The new watched status
     * @return Optional containing the updated movie, or empty if not found
     * @throws IllegalArgumentException if watched status is null
     */
    public Optional<Movie> updateWatchedStatus(Long movieId, Boolean watched) {
        if (watched == null) {
            throw new IllegalArgumentException("Watched status is required");
        }

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
    public Optional<Movie> updateRating(Long id, Integer rating) {
        if (rating == null) {
            throw new IllegalArgumentException("Rating is required");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        return movieRepository.findById(id)
                .map(movie -> {
                    movie.setRating(rating);
                    return movieRepository.save(movie);
                });
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