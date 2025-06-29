package com.example.moviewatchlist.config;

/**
 * Spring Web configuration for serving static resources.
 * Maps the local images directory to a public URL path so the frontend can access downloaded movie images.
 */
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Path to the directory where movie images are stored, loaded from application properties.
     */
    @Value("${movie.images.path}")
    private String imagesPath;

    /**
     * Adds a resource handler so images can be accessed via URLs like:
     * http://localhost:8080/movie-images/Inception_0.jpg
     *
     * @param registry the resource handler registry
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/movie-images/**")
                .addResourceLocations("file:" + imagesPath);
    }
}