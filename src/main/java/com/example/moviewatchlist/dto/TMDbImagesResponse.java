package com.example.moviewatchlist.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Response class for TMDb API images endpoint.
 * Contains lists of backdrop and poster images for a movie.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TMDbImagesResponse {

    /**
     * List of backdrop images.
     */
    private List<ImageData> backdrops;

    /**
     * List of poster images.
     */
    private List<ImageData> posters;

    /**
     * Gets the list of backdrop images.
     * @return list of backdrop images
     */
    public List<ImageData> getBackdrops() { return backdrops; }

    /**
     * Sets the list of backdrop images.
     * @param backdrops list of backdrop images
     */
    public void setBackdrops(List<ImageData> backdrops) { this.backdrops = backdrops; }

    /**
     * Gets the list of poster images.
     * @return list of poster images
     */
    public List<ImageData> getPosters() { return posters; }

    /**
     * Sets the list of poster images.
     * @param posters list of poster images
     */
    public void setPosters(List<ImageData> posters) { this.posters = posters; }

    /**
     * Represents individual image data from TMDb.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageData {
        /**
         * File path to the image on TMDb servers.
         */
        private String file_path;

        /**
         * Average rating for this image.
         */
        private Double vote_average;

        /**
         * Gets the file path to the image.
         * @return file path
         */
        public String getFile_path() { return file_path; }

        /**
         * Sets the file path to the image.
         * @param file_path file path
         */
        public void setFile_path(String file_path) { this.file_path = file_path; }

        /**
         * Gets the average rating for this image.
         * @return average rating
         */
        public Double getVote_average() { return vote_average; }

        /**
         * Sets the average rating for this image.
         * @param vote_average average rating
         */
        public void setVote_average(Double vote_average) { this.vote_average = vote_average; }
    }
}