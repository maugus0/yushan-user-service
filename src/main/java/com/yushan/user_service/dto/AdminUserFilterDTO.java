package com.yushan.user_service.dto;

import com.yushan.user_service.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserFilterDTO {
    // Pagination
    private int page = 0;
    private int size = 20;

    // Filters
    private UserStatus status;
    private Boolean isAdmin;
    private Boolean isAuthor;

    // Sorting
    private String sortBy = "createTime";
    private String sortOrder = "desc";
}