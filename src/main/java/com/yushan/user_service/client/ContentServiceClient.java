package com.yushan.user_service.client;

import com.yushan.user_service.client.dto.ChapterInfoDTO;
import com.yushan.user_service.client.dto.NovelInfoDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.yushan.user_service.dto.ApiResponse;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@FeignClient(name = "content-service", fallback = ContentServiceClient.ContentServiceFallback.class)
public interface ContentServiceClient {

    @PostMapping("/api/v1/novels/batch/get")
    @CircuitBreaker(name = "content-service")
    ApiResponse<List<NovelInfoDTO>> getNovelsByIds(@RequestBody List<Integer> novelIds);

    @PostMapping("/api/v1/chapters/batch/get")
    @CircuitBreaker(name = "content-service")
    ApiResponse<List<ChapterInfoDTO>> getChaptersByIds(@RequestBody List<Integer> chapterIds);

    @GetMapping("/api/v1/novels/{novelId}")
    @CircuitBreaker(name = "content-service")
    ApiResponse<NovelInfoDTO> getNovelById(@RequestParam("novelId") Integer novelId);

    /**
     * Fallback class for ContentServiceClient.
     * This class will be instantiated if the content-service is down or responses with an error.
     */
    @Component
    class ContentServiceFallback implements ContentServiceClient {
        private static final Logger logger = LoggerFactory.getLogger(ContentServiceFallback.class);

        @Override
        public ApiResponse<List<NovelInfoDTO>> getNovelsByIds(List<Integer> novelIds) {
            logger.error("Circuit breaker opened for content-service. Falling back for getNovelsByIds request with {} ids.", novelIds.size());
            // return an empty list as a fallback
            return ApiResponse.success(Collections.emptyList());
        }

        @Override
        public ApiResponse<List<ChapterInfoDTO>> getChaptersByIds(List<Integer> chapterIds) {
            logger.error("Circuit breaker opened for content-service. Falling back for getChaptersByIds request with {} ids.", chapterIds.size());
            // return an empty list as a fallback
            return ApiResponse.success(Collections.emptyList());
        }

        @Override
        public ApiResponse<NovelInfoDTO> getNovelById(@RequestParam("novelId") Integer novelId) {
            logger.error("Circuit breaker opened for content-service. Falling back for getNovelById request with {} id.", novelId);
            // return an empty list as a fallback
            return ApiResponse.success(null);
        }
    }
}