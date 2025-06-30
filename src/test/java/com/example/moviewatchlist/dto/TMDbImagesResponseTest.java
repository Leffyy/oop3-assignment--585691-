package com.example.moviewatchlist.dto;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TMDbImagesResponseTest {

    @Test
    void settersAndGettersWork() {
        // Test ImageData
        TMDbImagesResponse.ImageData image = new TMDbImagesResponse.ImageData();
        image.setFile_path("/path/to/image.jpg");
        image.setVote_average(8.5);

        assertEquals("/path/to/image.jpg", image.getFile_path());
        assertEquals(8.5, image.getVote_average());

        // Test TMDbImagesResponse
        TMDbImagesResponse response = new TMDbImagesResponse();
        response.setBackdrops(List.of(image));
        response.setPosters(List.of(image));

        assertEquals(1, response.getBackdrops().size());
        assertEquals(1, response.getPosters().size());
        assertSame(image, response.getBackdrops().get(0));
        assertSame(image, response.getPosters().get(0));
    }
}
