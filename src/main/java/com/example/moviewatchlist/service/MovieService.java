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
    
    // Add a new movie to the watchlist by searching for it online
    public CompletableFuture<Movie> addMovieToWatchlist(String title) {
        // First, try to find the movie using OMDb API
        return omdbService.getMovieData(title)
                .thenCompose(omdbResponse -> {
                    // Check if OMDb found the movie
                    if (!"True".equals(omdbResponse.getResponse())) {
                        throw new RuntimeException("Movie not found: " + omdbResponse.getError());
                    }
                    
                    // Make sure we don't add the same movie twice
                    if (movieRepository.existsByTitleAndReleaseYear(omdbResponse.getTitle(), omdbResponse.getYear())) {
                        throw new RuntimeException("Movie already exists in watchlist");
                    }
                    
                    // Create a new movie with the basic info from OMDb
                    Movie movie = new Movie();
                    movie.setTitle(omdbResponse.getTitle());
                    movie.setReleaseYear(omdbResponse.getYear());
                    movie.setDirector(omdbResponse.getDirector());
                    movie.setGenre(omdbResponse.getGenre());
                    movie.setPlot(omdbResponse.getPlot());
                    movie.setRuntime(omdbResponse.getRuntime());
                    movie.setImdbRating(omdbResponse.getImdbRating());
                    
                    // Now try to get more details from TMDb
                    return tmdbService.searchMovie(title)
                            .thenCompose(tmdbSearchResponse -> {
                                // If TMDb doesn't have the movie, just save what we have from OMDb
                                if (tmdbSearchResponse.getResults().isEmpty()) {
                                    return CompletableFuture.completedFuture(movieRepository.save(movie));
                                }
                                
                                // Add the extra info from TMDb
                                TMDbSearchResponse.TMDbMovie tmdbMovie = tmdbSearchResponse.getResults().get(0);
                                movie.setTmdbId(tmdbMovie.getId());
                                movie.setOverview(tmdbMovie.getOverview());
                                movie.setReleaseDate(tmdbMovie.getReleaseDate());
                                movie.setVoteAverage(tmdbMovie.getVoteAverage());
                                
                                // Get images and similar movies at the same time to save time
                                CompletableFuture<TMDbImagesResponse> imagesFuture = 
                                    tmdbService.getMovieImages(tmdbMovie.getId());
                                CompletableFuture<TMDbSimilarResponse> similarFuture = 
                                    tmdbService.getSimilarMovies(tmdbMovie.getId());
                                
                                return CompletableFuture.allOf(imagesFuture, similarFuture)
                                        .thenCompose(v -> {
                                            TMDbImagesResponse imagesResponse = imagesFuture.join();
                                            TMDbSimilarResponse similarResponse = similarFuture.join();
                                            
                                            // Save up to 10 similar movies
                                            List<String> similarMovies = new ArrayList<>();
                                            if (similarResponse.getResults() != null) {
                                                similarMovies = similarResponse.getResults().stream()
                                                        .map(TMDbSimilarResponse.SimilarMovie::getTitle)
                                                        .limit(10)
                                                        .toList();
                                            }
                                            movie.setSimilarMovies(similarMovies);
                                            
                                            // Collect image paths to download (2 posters + 1 backdrop)
                                            List<String> imagePaths = new ArrayList<>();
                                            if (imagesResponse.getPosters() != null && !imagesResponse.getPosters().isEmpty()) {
                                                imagePaths.addAll(imagesResponse.getPosters().stream()
                                                        .map(TMDbImagesResponse.ImageData::getFile_path)
                                                        .limit(2)
                                                        .toList());
                                            }
                                            if (imagesResponse.getBackdrops() != null && !imagesResponse.getBackdrops().isEmpty()) {
                                                imagePaths.addAll(imagesResponse.getBackdrops().stream()
                                                        .map(TMDbImagesResponse.ImageData::getFile_path)
                                                        .limit(1)
                                                        .toList());
                                            }
                                            
                                            // Download the images if we found any
                                            if (!imagePaths.isEmpty()) {
                                                return imageDownloadService.downloadImages(imagePaths, movie.getTitle())
                                                        .thenApply(downloadedPaths -> {
                                                            movie.setImagePaths(downloadedPaths);
                                                            return movieRepository.save(movie);
                                                        });
                                            } else {
                                                // No images found, just save the movie
                                                return CompletableFuture.completedFuture(movieRepository.save(movie));
                                            }
                                        });
                            });
                });
    }
    
    // Get a page of movies from the watchlist
    public PaginatedResponse<MovieResponse> getMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> moviePage = movieRepository.findAll(pageable);
        
        // Convert movies to response format
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
    
    // Mark a movie as watched or unwatched
    public Optional<Movie> updateWatchedStatus(Long movieId, boolean watched) {
        Optional<Movie> movieOpt = movieRepository.findById(movieId);
        if (movieOpt.isPresent()) {
            Movie movie = movieOpt.get();
            movie.setWatched(watched);
            return Optional.of(movieRepository.save(movie));
        }
        return Optional.empty();
    }
    
    // Give a movie a rating from 1 to 5 stars
    public Optional<Movie> updateRating(Long movieId, Integer rating) {
        // Make sure the rating is valid
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
    
    // Remove a movie from the watchlist
    public boolean deleteMovie(Long movieId) {
        if (movieRepository.existsById(movieId)) {
            movieRepository.deleteById(movieId);
            return true;
        }
        return false;
    }
    
    // Get details for a specific movie
    public Optional<MovieResponse> getMovieById(Long movieId) {
        return movieRepository.findById(movieId)
                .map(MovieResponse::new);
    }
}