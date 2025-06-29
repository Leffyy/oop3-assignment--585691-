package com.example.moviewatchlist.config;

/**
 * Configuration for running tasks asynchronously in the Movie Watchlist app.
 * Sets up a thread pool for background task execution.
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

/**
 * Configuration for running tasks asynchronously in the Movie Watchlist app.
 * Sets up a thread pool for background task execution.
 */
@Configuration
@EnableAsync 
public class AsyncConfig {

    /**
     * Creates a thread pool for handling background tasks.
     *
     * @return the configured Executor for async tasks
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("MovieWatchlist-");
        executor.initialize();
        return executor;
    }
}
