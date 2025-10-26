package com.yushan.user_service.controller;

import com.yushan.user_service.dao.UserMapper;
import com.yushan.user_service.dto.*;
import com.yushan.user_service.entity.User;
import com.yushan.user_service.exception.UnauthorizedException;
import com.yushan.user_service.exception.ValidationException;
import com.yushan.user_service.security.CustomUserDetailsService.CustomUserDetails;
import com.yushan.user_service.service.AuthorService;
import com.yushan.user_service.service.MailService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/author")
public class AuthorController {

    @Autowired
    private MailService mailService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AuthorService authorService;

    /**
     * Send email verification for author upgrade
     */
    @PostMapping("/send-email-author-verification")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> sendEmailAuthorVerification(@Valid @RequestBody EmailVerificationRequestDTO emailRequest) {
        String email = emailRequest.getEmail();

        //query email if exists
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new ValidationException("User not found");
        }

        // Check if user is already an author
        if (user.getIsAuthor() != null && user.getIsAuthor()) {
            throw new ValidationException("User is already an author");
        }

        mailService.sendVerificationCode(email);

        return ApiResponse.success("Verification code sent successfully");
    }

    /**
     * Upgrade user to author with verification code
     */
    @PostMapping("/upgrade-to-author")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserProfileResponseDTO> upgradeToAuthor(
            @Valid @RequestBody AuthorUpgradeRequestDTO request,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Authentication required");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        try {
            UserProfileResponseDTO userProfile = authorService.upgradeToAuthor(email, request.getVerificationCode());
            return ApiResponse.success("User upgraded to author successfully", userProfile);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(e.getMessage());
        } catch (Exception e) {
            throw new ValidationException("Failed to upgrade user to author: " + e.getMessage());
        }
    }
}
