package com.example.moviewatchlist;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class MovieWatchlistApplicationTest {

    @Test
    void mainRunsWithoutException() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(any(Class.class), any(String[].class))).thenReturn(null);
            MovieWatchlistApplication.main(new String[] {});
        }
    }
}
