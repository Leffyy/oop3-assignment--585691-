package com.example.moviewatchlist.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${movie.images.path}")
    private String imagesPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // This allows the frontend to access images via URLs like:
        // http://localhost:8080/movie-images/Inception_0.jpg
        registry.addResourceHandler("/movie-images/**")
                .addResourceLocations("file:" + imagesPath);
    }
}