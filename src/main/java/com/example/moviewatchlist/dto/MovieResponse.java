package com.example.moviewatchlist.dto;

import com.example.moviewatchlist.model.Movie;
import java.util.List;

public class MovieResponse {
    private Long id;
    private String title;
    private String year;
    private String director;
    private String genre;
    private String plot;
    private String runtime;
    private String imdbRating;
    private String overview;
    private String releaseDate;
    private Double voteAverage;
    private List<String> imagePaths;
    private List<String> similarMovies;
    private boolean watched;
    private Integer rating;
    
    public MovieResponse(Movie movie) {
        this.id = movie.getId();
        this.title = movie.getTitle();
        this.year = movie.getYear();
        this.director = movie.getDirector();
        this.genre = movie.getGenre();
        this.plot = movie.getPlot();
        this.runtime = movie.getRuntime();
        this.imdbRating = movie.getImdbRating();
        this.overview = movie.getOverview();
        this.releaseDate = movie.getReleaseDate();
        this.voteAverage = movie.getVoteAverage();
        this.imagePaths = movie.getImagePaths();
        this.similarMovies = movie.getSimilarMovies();
        this.watched = movie.isWatched();
        this.rating = movie.getRating();
    }
    
    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getYear() { return year; }
    public String getDirector() { return director; }
    public String getGenre() { return genre; }
    public String getPlot() { return plot; }
    public String getRuntime() { return runtime; }
    public String getImdbRating() { return imdbRating; }
    public String getOverview() { return overview; }
    public String getReleaseDate() { return releaseDate; }
    public Double getVoteAverage() { return voteAverage; }
    public List<String> getImagePaths() { return imagePaths; }
    public List<String> getSimilarMovies() { return similarMovies; }
    public boolean isWatched() { return watched; }
    public Integer getRating() { return rating; }
}