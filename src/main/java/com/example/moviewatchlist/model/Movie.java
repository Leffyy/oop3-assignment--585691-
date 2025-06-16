package com.example.moviewatchlist.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;

// This class represents a movie in our database
@Entity
@Table(name = "movies")
public class Movie {
    // Unique ID for each movie, automatically generated
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Basic movie information that we always need
    @Column(nullable = false)
    private String title;
    @Column(name = "movie_year")
    private String releaseYear;
    private String director;
    private String genre;
    private String plot;
    private String runtime;
    private String imdbRating;
    
    // Extra info we get from TMDB API
    private Integer tmdbId;
    private String overview;
    private String releaseDate;
    private Double voteAverage;
    
    // Store multiple image URLs for each movie
    @ElementCollection
    @CollectionTable(name = "movie_images", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "image_path")
    private List<String> imagePaths;
    
    // Store titles of similar movies
    @ElementCollection
    @CollectionTable(name = "similar_movies", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "similar_movie_title")
    private List<String> similarMovies;
    
    // User's personal tracking info
    private boolean watched = false;
    
    // User can rate movies from 1 to 5 stars
    @Column(nullable = true)
    private Integer rating;
    
    // Empty constructor needed by JPA
    public Movie() {}
    
    // Constructor for creating a new movie with basic info
    public Movie(String title, String releaseYear, String director, String genre) {
        this.title = title;
        this.releaseYear = releaseYear;
        this.director = director;
        this.genre = genre;
    }
    
    // Simple getters and setters for accessing movie data
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getReleaseYear() { return releaseYear; }
    public void setReleaseYear(String releaseYear) { this.releaseYear = releaseYear; }
    
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    
    public String getPlot() { return plot; }
    public void setPlot(String plot) { this.plot = plot; }
    
    public String getRuntime() { return runtime; }
    public void setRuntime(String runtime) { this.runtime = runtime; }
    
    public String getImdbRating() { return imdbRating; }
    public void setImdbRating(String imdbRating) { this.imdbRating = imdbRating; }
    
    public Integer getTmdbId() { return tmdbId; }
    public void setTmdbId(Integer tmdbId) { this.tmdbId = tmdbId; }
    
    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }
    
    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    
    public Double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(Double voteAverage) { this.voteAverage = voteAverage; }
    
    public List<String> getImagePaths() { return imagePaths; }
    public void setImagePaths(List<String> imagePaths) { this.imagePaths = imagePaths; }
    
    public List<String> getSimilarMovies() { return similarMovies; }
    public void setSimilarMovies(List<String> similarMovies) { this.similarMovies = similarMovies; }
    
    public boolean isWatched() { return watched; }
    public void setWatched(boolean watched) { this.watched = watched; }
    
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    
    public String getYear() { return releaseYear; }
    public void setYear(String year) { this.releaseYear = year; }
    
    // Check if two movies are the same based on id and title
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return Objects.equals(id, movie.id) && Objects.equals(title, movie.title);
    }
    
    // Generate hash code for this movie
    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }
}