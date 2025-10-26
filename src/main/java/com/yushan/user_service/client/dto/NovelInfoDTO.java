package com.yushan.user_service.client.dto;

public record NovelInfoDTO(
        Integer id,
        String title,
        String authorUsername,
        String coverImgUrl,
        Integer chapterCnt,
        String status
) {}
