package com.yushan.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for admin promotion request
 * Used for POST /api/admin/promote-to-admin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminPromoteRequestDTO {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
}
