package com.example.moviewatchlist.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the response from the OMDb API.
 * Maps JSON fields to Java properties for easy access.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OMDbResponse {
    /**
     * Movie title from the API.
     */
    @JsonProperty("Title")
    private String title;

    /**
     * Release year of the movie.
     */
    @JsonProperty("Year")
    private String year;

    /**
     * Director's name.
     */
    @JsonProperty("Director")
    private String director;

    /**
     * Movie genres (like "Action, Drama").
     */
    @JsonProperty("Genre")
    private String genre;

    /**
     * Short description of the movie plot.
     */
    @JsonProperty("Plot")
    private String plot;

    /**
     * How long the movie is (like "120 min").
     */
    @JsonProperty("Runtime")
    private String runtime;

    /**
     * IMDb rating score.
     */
    @JsonProperty("imdbRating")
    private String imdbRating;

    /**
     * API response status ("True" or "False").
     */
    @JsonProperty("Response")
    private String response;

    /**
     * Error message if something went wrong.
     */
    @JsonProperty("Error")
    private String error;

    /** Gets the movie title. */
    public String getTitle() { return title; }
    /** Sets the movie title. */
    public void setTitle(String title) { this.title = title; }

    /** Gets the release year. */
    public String getYear() { return year; }
    /** Sets the release year. */
    public void setYear(String year) { this.year = year; }

    /** Gets the director's name. */
    public String getDirector() { return director; }
    /** Sets the director's name. */
    public void setDirector(String director) { this.director = director; }

    /** Gets the movie genres. */
    public String getGenre() { return genre; }
    /** Sets the movie genres. */
    public void setGenre(String genre) { this.genre = genre; }

    /** Gets the movie plot. */
    public String getPlot() { return plot; }
    /** Sets the movie plot. */
    public void setPlot(String plot) { this.plot = plot; }

    /** Gets the runtime. */
    public String getRuntime() { return runtime; }
    /** Sets the runtime. */
    public void setRuntime(String runtime) { this.runtime = runtime; }

    /** Gets the IMDb rating. */
    public String getImdbRating() { return imdbRating; }
    /** Sets the IMDb rating. */
    public void setImdbRating(String imdbRating) { this.imdbRating = imdbRating; }

    /** Gets the API response status. */
    public String getResponse() { return response; }
    /** Sets the API response status. */
    public void setResponse(String response) { this.response = response; }

    /** Gets the error message. */
    public String getError() { return error; }
    /** Sets the error message. */
    public void setError(String error) { this.error = error; }
}