package com.yushan.user_service.event;

import com.yushan.user_service.client.ContentServiceClient;
import com.yushan.user_service.client.dto.ChapterInfoDTO;
import com.yushan.user_service.client.dto.NovelInfoDTO;
import com.yushan.user_service.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContentServiceClientFallbackTest {

    private ContentServiceClient.ContentServiceFallback fallback;

    @BeforeEach
    void setUp() {
        fallback = new ContentServiceClient.ContentServiceFallback();
    }

    @Test
    void getNovelsByIds_shouldReturnSuccessWithEmptyList() {
        // Given
        List<Integer> novelIds = Arrays.asList(1, 2, 3);

        // When
        ApiResponse<List<NovelInfoDTO>> response = fallback.getNovelsByIds(novelIds);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    void getChaptersByIds_shouldReturnSuccessWithEmptyList() {
        // Given
        List<Integer> chapterIds = Arrays.asList(4, 5, 6);

        // When
        ApiResponse<List<ChapterInfoDTO>> response = fallback.getChaptersByIds(chapterIds);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    void getNovelById_shouldReturnSuccessWithNullData() {
        // Given
        Integer novelId = 101;

        // When
        ApiResponse<NovelInfoDTO> response = fallback.getNovelById(novelId);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNull(response.getData());
    }
}
