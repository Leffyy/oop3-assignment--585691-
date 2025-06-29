package com.example.moviewatchlist.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents the response from TMDb API when searching for movies.
 * Contains a list of movie search results.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TMDbSearchResponse {

    /**
     * The "results" field from the JSON response contains a list of movies.
     */
    @JsonProperty("results")
    private List<TMDbMovie> results;

    /**
     * Gets the list of TMDb movie results.
     * @return list of TMDbMovie objects
     */
    public List<TMDbMovie> getResults() { return results; }

    /**
     * Sets the list of TMDb movie results.
     * @param results list of TMDbMovie objects
     */
    public void setResults(List<TMDbMovie> results) { this.results = results; }

    /**
     * Represents a single movie from the TMDb search results.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TMDbMovie {
        /**
         * Unique movie ID from TMDb.
         */
        private Integer id;
        /**
         * Movie title.
         */
        private String title;
        /**
         * Movie overview/description.
         */
        private String overview;
        /**
         * Release date (mapped from "release_date" in JSON).
         */
        @JsonProperty("release_date")
        private String releaseDate;
        /**
         * Average vote/rating (mapped from "vote_average" in JSON).
         */
        @JsonProperty("vote_average")
        private Double voteAverage;
        /**
         * Poster image path (mapped from "poster_path" in JSON).
         */
        @JsonProperty("poster_path")
        private String posterPath;

        /**
         * Gets the movie ID.
         * @return movie ID
         */
        public Integer getId() { return id; }
        /**
         * Sets the movie ID.
         * @param id movie ID
         */
        public void setId(Integer id) { this.id = id; }

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

        /**
         * Gets the movie overview.
         * @return movie overview
         */
        public String getOverview() { return overview; }
        /**
         * Sets the movie overview.
         * @param overview movie overview
         */
        public void setOverview(String overview) { this.overview = overview; }

        /**
         * Gets the release date.
         * @return release date
         */
        public String getReleaseDate() { return releaseDate; }
        /**
         * Sets the release date.
         * @param releaseDate release date
         */
        public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

        /**
         * Gets the average vote.
         * @return average vote
         */
        public Double getVoteAverage() { return voteAverage; }
        /**
         * Sets the average vote.
         * @param voteAverage average vote
         */
        public void setVoteAverage(Double voteAverage) { this.voteAverage = voteAverage; }

        /**
         * Gets the poster path.
         * @return poster path
         */
        public String getPosterPath() { return posterPath; }
        /**
         * Sets the poster path.
         * @param posterPath poster path
         */
        public void setPosterPath(String posterPath) { this.posterPath = posterPath; }
    }
}