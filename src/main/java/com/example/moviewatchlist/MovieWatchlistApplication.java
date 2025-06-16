package com.example.moviewatchlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MovieWatchlistApplication {
    //main method to start the Spring Boot application
    public static void main(String[] args) {
        SpringApplication.run(MovieWatchlistApplication.class, args);
    }
}