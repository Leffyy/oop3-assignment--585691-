package com.example.moviewatchlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

// This tells Spring Boot that this is the main application class
@SpringBootApplication
// This enables asynchronous processing in the application
@EnableAsync
public class MovieWatchlistApplication {
    // This is the entry point of the application - where everything starts
    public static void main(String[] args) {
        // Start the Spring Boot application
        SpringApplication.run(MovieWatchlistApplication.class, args);
    }
}