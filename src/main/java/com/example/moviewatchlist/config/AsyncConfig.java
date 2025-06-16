package com.example.moviewatchlist.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

// Configuration for running tasks asynchronously in the Movie Watchlist app
@Configuration
@EnableAsync // Enables async method calls
public class AsyncConfig {
    
    // Creates a thread pool for handling background tasks
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Always keep 5 threads running
        executor.setCorePoolSize(5);
        
        // Allow up to 10 threads maximum
        executor.setMaxPoolSize(10);
        
        // Queue up to 100 tasks if all threads are busy
        executor.setQueueCapacity(100);
        
        // Name threads for easier debugging
        executor.setThreadNamePrefix("MovieWatchlist-");
        
        executor.initialize();
        
        return executor;
    }
}
