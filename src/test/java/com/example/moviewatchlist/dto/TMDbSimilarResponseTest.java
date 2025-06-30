package com.example.moviewatchlist.dto;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TMDbSimilarResponseTest {

    @Test
    void settersAndGettersWork() {
        // Test SimilarMovie
        TMDbSimilarResponse.SimilarMovie movie = new TMDbSimilarResponse.SimilarMovie();
        movie.setId(42);
        movie.setTitle("The Matrix");

        assertEquals(42, movie.getId());
        assertEquals("The Matrix", movie.getTitle());

        // Test TMDbSimilarResponse
        TMDbSimilarResponse response = new TMDbSimilarResponse();
        response.setResults(List.of(movie));

        assertEquals(1, response.getResults().size());
        assertSame(movie, response.getResults().get(0));
    }
}
