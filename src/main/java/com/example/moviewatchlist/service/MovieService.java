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
    
    public CompletableFuture<Movie> addMovieToWatchlist(String title) {
        // Check if movie already exists
        return omdbService.getMovieData(title)
                .thenCompose(omdbResponse -> {
                    if (!"True".equals(omdbResponse.getResponse())) {
                        throw new RuntimeException("Movie not found: " + omdbResponse.getError());
                    }
                    
                    // Check if movie already exists in database
                    if (movieRepository.existsByTitleAndYear(omdbResponse.getTitle(), omdbResponse.getYear())) {
                        throw new RuntimeException("Movie already exists in watchlist");
                    }
                    
                    // Create movie with OMDb data
                    Movie movie = new Movie();
                    movie.setTitle(omdbResponse.getTitle());
                    movie.setYear(omdbResponse.getYear());
                    movie.setDirector(omdbResponse.getDirector());
                    movie.setGenre(omdbResponse.getGenre());
                    movie.setPlot(omdbResponse.getPlot());
                    movie.setRuntime(omdbResponse.getRuntime());
                    movie.setImdbRating(omdbResponse.getImdbRating());
                    
                    // Search for movie in TMDb
                    return tmdbService.searchMovie(title)
                            .thenCompose(tmdbSearchResponse -> {
                                if (tmdbSearchResponse.getResults().isEmpty()) {
                                    // Save movie with only OMDb data
                                    return CompletableFuture.completedFuture(movieRepository.save(movie));
                                }
                                
                                TMDbSearchResponse.TMDbMovie tmdbMovie = tmdbSearchResponse.getResults().get(0);
                                movie.setTmdbId(tmdbMovie.getId());
                                movie.setOverview(tmdbMovie.getOverview());
                                movie.setReleaseDate(tmdbMovie.getReleaseDate());
                                movie.setVoteAverage(tmdbMovie.getVoteAverage());
                                
                                // Parallel calls for images and similar movies
                                CompletableFuture<TMDbImagesResponse> imagesFuture = 
                                    tmdbService.getMovieImages(tmdbMovie.getId());
                                CompletableFuture<TMDbSimilarResponse> similarFuture = 
                                    tmdbService.getSimilarMovies(tmdbMovie.getId());
                                
                                return CompletableFuture.allOf(imagesFuture, similarFuture)
                                        .thenCompose(v -> {
                                            TMDbImagesResponse imagesResponse = imagesFuture.join();
                                            TMDbSimilarResponse similarResponse = similarFuture.join();
                                            
                                            // Process similar movies
                                            List<String> similarMovies = new ArrayList<>();
                                            if (similarResponse.getResults() != null) {
                                                similarMovies = similarResponse.getResults().stream()
                                                        .map(TMDbSimilarResponse.SimilarMovie::getTitle)
                                                        .limit(10)
                                                        .toList();
                                            }
                                            movie.setSimilarMovies(similarMovies);
                                            
                                            // Process and download images
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
                                            
                                            if (!imagePaths.isEmpty()) {
                                                return imageDownloadService.downloadImages(imagePaths, movie.getTitle())
                                                        .thenApply(downloadedPaths -> {
                                                            movie.setImagePaths(downloadedPaths);
                                                            return movieRepository.save(movie);
                                                        });
                                            } else {
                                                return CompletableFuture.completedFuture(movieRepository.save(movie));
                                            }
                                        });
                            });
                });
    }
    
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
    
    public Optional<Movie> updateWatchedStatus(Long movieId, boolean watched) {
        Optional<Movie> movieOpt = movieRepository.findById(movieId);
        if (movieOpt.isPresent()) {
            Movie movie = movieOpt.get();
            movie.setWatched(watched);
            return Optional.of(movieRepository.save(movie));
        }
        return Optional.empty();
    }
    
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
    
    public boolean deleteMovie(Long movieId) {
        if (movieRepository.existsById(movieId)) {
            movieRepository.deleteById(movieId);
            return true;
        }
        return false;
    }
    
    public Optional<MovieResponse> getMovieById(Long movieId) {
        return movieRepository.findById(movieId)
                .map(MovieResponse::new);
    }
}