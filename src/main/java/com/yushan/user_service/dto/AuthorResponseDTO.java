package com.yushan.user_service.dto;

import lombok.Data;

@Data
public class AuthorResponseDTO {
    private String uuid;
    private String username;
    private String avatarUrl;
    private Integer novelNum;
    private long totalVoteCnt;
    private Long totalViewCnt;
}
