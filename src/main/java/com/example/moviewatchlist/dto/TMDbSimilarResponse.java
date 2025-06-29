package com.example.moviewatchlist.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Response object for TMDb API similar movies endpoint.
 * Contains a list of similar movies.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TMDbSimilarResponse {

    /**
     * List of similar movies returned by the API.
     */
    private List<SimilarMovie> results;

    /**
     * Gets the list of similar movies.
     * @return list of similar movies
     */
    public List<SimilarMovie> getResults() { return results; }

    /**
     * Sets the list of similar movies.
     * @param results list of similar movies
     */
    public void setResults(List<SimilarMovie> results) { this.results = results; }

    /**
     * Individual movie data within the similar movies response.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SimilarMovie {
        /**
         * Unique movie ID from TMDb.
         */
        private int id;
        /**
         * Movie title.
         */
        private String title;

        /**
         * Gets the movie ID.
         * @return movie ID
         */
        public int getId() { return id; }

        /**
         * Sets the movie ID.
         * @param id movie ID
         */
        public void setId(int id) { this.id = id; }

        /**
         * Gets the movie title.
         * @return movie title
         */
        public String getTitle() { return title; }

        /**
         * Sets the movie title.
         * @param title movie title
         */
        public void setTitle(String title) { this.title = title; }
    }
}