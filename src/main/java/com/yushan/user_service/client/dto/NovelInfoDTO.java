package com.yushan.user_service.client.dto;

public record NovelInfoDTO(
        Integer id,
        String novelTitle,
        String novelAuthor,
        String novelCover,
        Integer chapterCnt,
        String status
) {}
