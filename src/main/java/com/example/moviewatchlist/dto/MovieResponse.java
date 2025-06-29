package com.example.moviewatchlist.dto;

import com.example.moviewatchlist.model.Movie;
import java.util.List;

/**
 * Represents a movie response that gets sent back to the client.
 * Contains all relevant movie information, including user-specific data and related movies.
 */
public class MovieResponse {
    /** Movie ID. */
    private Long id;
    /** Movie title. */
    private String title;
    /** Release year. */
    private String year;
    /** Director's name. */
    private String director;
    /** Movie genre(s). */
    private String genre;
    /** Movie plot/description. */
    private String plot;
    /** Movie runtime (e.g., "120 min"). */
    private String runtime;
    /** IMDb rating score. */
    private String imdbRating;
    /** TMDb overview/description. */
    private String overview;
    /** Release date (from TMDb). */
    private String releaseDate;
    /** Average vote/rating (from TMDb). */
    private Double voteAverage;
    /** List of image paths for the movie. */
    private List<String> imagePaths;
    /** List of similar movie titles. */
    private List<String> similarMovies;
    /** Whether the user has marked this movie as watched. */
    private boolean watched;
    /** User's rating for this movie (1-5 stars, or null if not rated). */
    private Integer rating;

    /**
     * Constructs a MovieResponse by copying data from a Movie entity.
     * @param movie the Movie entity to copy data from
     */
    public MovieResponse(Movie movie) {
        this.id = movie.getId();
        this.title = movie.getTitle();
        this.year = movie.getReleaseYear();
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
        this.watched = Boolean.TRUE.equals(movie.getWatched());
        this.rating = movie.getRating();
    }

    /** @return the movie ID */
    public Long getId() { return id; }
    /** @return the movie title */
    public String getTitle() { return title; }
    /** @return the release year */
    public String getReleaseYear() { return year; }
    /** @return the director's name */
    public String getDirector() { return director; }
    /** @return the genre(s) */
    public String getGenre() { return genre; }
    /** @return the plot/description */
    public String getPlot() { return plot; }
    /** @return the runtime */
    public String getRuntime() { return runtime; }
    /** @return the IMDb rating */
    public String getImdbRating() { return imdbRating; }
    /** @return the TMDb overview */
    public String getOverview() { return overview; }
    /** @return the release date */
    public String getReleaseDate() { return releaseDate; }
    /** @return the average vote/rating */
    public Double getVoteAverage() { return voteAverage; }
    /** @return the list of image paths */
    public List<String> getImagePaths() { return imagePaths; }
    /** @return the list of similar movies */
    public List<String> getSimilarMovies() { return similarMovies; }
    /** @return true if the movie is marked as watched */
    public boolean isWatched() { return watched; }
    /** @return the user's rating for this movie */
    public Integer getRating() { return rating; }
}