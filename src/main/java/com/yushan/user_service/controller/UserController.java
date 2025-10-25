package com.yushan.user_service.controller;

import com.yushan.user_service.dao.UserMapper;
import com.yushan.user_service.dto.*;
import com.yushan.user_service.entity.User;
import com.yushan.user_service.exception.ForbiddenException;
import com.yushan.user_service.exception.UnauthorizedException;
import com.yushan.user_service.exception.ValidationException;
import com.yushan.user_service.security.CustomUserDetailsService;
import com.yushan.user_service.service.UserService;
import com.yushan.user_service.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Return current authenticated user's profile
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserProfileResponseDTO> getCurrentUserProfile(Authentication authentication) {
        //get user id from authentication
        UUID userId = getCurrentUserId(authentication);

        UserProfileResponseDTO dto = userService.getUserProfile(userId);
        return ApiResponse.success("User profile retrieved successfully", dto);
    }

    /**
     * Update current user's editable profile fields
     */
    @PutMapping("/{id}/profile")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserProfileUpdateResponseDTO> updateProfile(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UserProfileUpdateRequestDTO body,
            Authentication authentication) {

        //get user id from authentication
        UUID userId = getCurrentUserId(authentication);

        if (!userId.equals(id)) {
            throw new ForbiddenException("Access denied");
        }

        try {
            UserProfileUpdateResponseDTO updateResponse = userService.updateUserProfileSelective(id, body);
            if (updateResponse == null) {
                throw new ValidationException("User not found");
            }
            
            // If email was changed, generate new tokens
            if (updateResponse.isEmailChanged()) {
                // Get updated user from database
                User updatedUser = userMapper.selectByPrimaryKey(id);
                if (updatedUser != null) {
                    // Generate new tokens with updated email
                    String newAccessToken = jwtUtil.generateAccessToken(updatedUser);
                    String newRefreshToken = jwtUtil.generateRefreshToken(updatedUser);
                    
                    // Set new tokens in response
                    updateResponse.setAccessToken(newAccessToken);
                    updateResponse.setRefreshToken(newRefreshToken);
                    updateResponse.setTokenType("Bearer");
                    updateResponse.setExpiresIn(jwtUtil.getAccessTokenExpiration());
                }
            }
            
            return ApiResponse.success("Profile updated successfully", updateResponse);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    /**
     * Send verification email for email change
     */
    @PostMapping("/send-email-change-verification")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> sendEmailChangeVerification(
            @RequestBody EmailVerificationRequestDTO emailRequest,
            Authentication authentication) {
        
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new UnauthorizedException("Authentication required");
            }

            String newEmail = emailRequest.getEmail();
            if (newEmail == null || newEmail.trim().isEmpty()) {
                throw new ValidationException("Email is required");
            }

            // Basic email format validation
            if (!newEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new ValidationException("Invalid email format");
            }

            userService.sendEmailChangeVerification(newEmail.trim().toLowerCase(java.util.Locale.ROOT));

            return ApiResponse.success("Verification code sent successfully");
        } catch (IllegalArgumentException e) {
            throw new ValidationException(e.getMessage());
        } catch (Exception e) {
            throw new ValidationException("Failed to send verification email: " + e.getMessage());
        }
    }

    /**
     * get a user's profile
     */
    @GetMapping("/{userId}/profile")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserProfileResponseDTO> getUserDetail(@PathVariable UUID userId) {
        UserProfileResponseDTO dto = userService.getUserProfile(userId);
        if (dto == null) {
            throw new ValidationException("User not found");
        }
        return ApiResponse.success("User profile retrieved successfully", dto);
    }

    /**
     * get current user id from authentication
     */
    protected UUID getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Authentication required");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetailsService.CustomUserDetails) {
            String id = ((CustomUserDetailsService.CustomUserDetails) principal).getUserId();
            if (id != null) {
                return UUID.fromString(id);
            } else {
                throw new ValidationException("User ID not found");
            }
        }
        throw new UnauthorizedException("Invalid authentication");
    }
}