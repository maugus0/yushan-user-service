package com.yushan.user_service.dto;

import com.yushan.user_service.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUpdateUserDTO {
    @NotNull(message = "Status cannot be null")
    private UserStatus status;
}