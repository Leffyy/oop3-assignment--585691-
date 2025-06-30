package com.example.moviewatchlist.dto;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaginatedResponseTest {

    @Test
    void constructorAndGettersWork() {
        List<String> content = List.of("a", "b", "c");
        int pageNumber = 0;
        int pageSize = 3;
        long totalElements = 9;
        int totalPages = 3;

        PaginatedResponse<String> response = new PaginatedResponse<>(content, pageNumber, pageSize, totalElements, totalPages);

        assertEquals(content, response.getContent());
        assertEquals(pageNumber, response.getPageNumber());
        assertEquals(pageSize, response.getPageSize());
        assertEquals(totalElements, response.getTotalElements());
        assertEquals(totalPages, response.getTotalPages());
        assertTrue(response.isFirst());
        assertFalse(response.isLast());

        // Test last page
        PaginatedResponse<String> lastPage = new PaginatedResponse<>(content, totalPages - 1, pageSize, totalElements, totalPages);
        assertTrue(lastPage.isLast());
        assertFalse(lastPage.isFirst());
    }
}
