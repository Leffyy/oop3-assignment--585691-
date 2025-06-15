package com.example.moviewatchlist.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    private String year;
    private String director;
    private String genre;
    private String plot;
    private String runtime;
    private String imdbRating;
    
    // TMDB specific fields
    private Integer tmdbId;
    private String overview;
    private String releaseDate;
    private Double voteAverage;
    
    @ElementCollection
    @CollectionTable(name = "movie_images", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "image_path")
    private List<String> imagePaths;
    
    @ElementCollection
    @CollectionTable(name = "similar_movies", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "similar_movie_title")
    private List<String> similarMovies;
    
    private boolean watched = false;
    
    @Column(nullable = true)
    private Integer rating; // 1-5 scale
    
    // Constructors
    public Movie() {}
    
    public Movie(String title, String year, String director, String genre) {
        this.title = title;
        this.year = year;
        this.director = director;
        this.genre = genre;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    
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
}