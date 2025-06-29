package com.example.moviewatchlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Entry point for the Movie Watchlist Spring Boot application.
 * Enables asynchronous processing.
 */
@SpringBootApplication
@EnableAsync
public class MovieWatchlistApplication {

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(MovieWatchlistApplication.class, args);
    }
}