package com.example.moviewatchlist.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// This class represents the response we get from the OMDb API
// It maps JSON fields to Java properties for easy access
@JsonIgnoreProperties(ignoreUnknown = true)
public class OMDbResponse {
    // Movie title from the API
    @JsonProperty("Title")
    private String title;
    
    // Release year of the movie
    @JsonProperty("Year")
    private String year;
    
    // Director's name
    @JsonProperty("Director")
    private String director;
    
    // Movie genres (like "Action, Drama")
    @JsonProperty("Genre")
    private String genre;   
    
    // Short description of the movie plot
    @JsonProperty("Plot")
    private String plot;
    
    // How long the movie is (like "120 min")
    @JsonProperty("Runtime")
    private String runtime;
    
    // IMDb rating score
    @JsonProperty("imdbRating")
    private String imdbRating;
    
    // API response status ("True" or "False")
    @JsonProperty("Response")
    private String response;
    
    // Error message if something went wrong
    @JsonProperty("Error")
    private String error;
    
    // Standard getters and setters for all properties
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
    
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}