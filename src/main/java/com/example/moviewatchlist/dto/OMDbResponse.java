package com.example.moviewatchlist.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OMDbResponse {
    @JsonProperty("Title")
    private String title;
    
    @JsonProperty("Year")
    private String year;
    
    @JsonProperty("Director")
    private String director;
    
    @JsonProperty("Genre")
    private String genre;
    
    @JsonProperty("Plot")
    private String plot;
    
    @JsonProperty("Runtime")
    private String runtime;
    
    @JsonProperty("imdbRating")
    private String imdbRating;
    
    @JsonProperty("Response")
    private String response;
    
    @JsonProperty("Error")
    private String error;
    
    // Getters and Setters
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