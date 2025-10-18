package com.yushan.user_service.controller;

import com.yushan.user_service.dao.UserMapper;
import com.yushan.user_service.dto.*;
import com.yushan.user_service.entity.User;
import com.yushan.user_service.exception.ValidationException;
import com.yushan.user_service.service.AuthService;
import com.yushan.user_service.service.MailService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Validated
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserMapper userMapper;


    @GetMapping("/test")
    public String test() {
        log.info("test");
        return "test";
    }

    /**
     * verifyEmail & Register a new user
     * @param registrationDTO
     * @return
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserRegistrationResponseDTO> register(@Valid @RequestBody UserRegistrationRequestDTO registrationDTO) {
        // no need to check if email exists here since we check it in register()
        boolean isValid = mailService.verifyEmail(registrationDTO.getEmail(), registrationDTO.getCode());

        if (!isValid) {
            throw new ValidationException("Invalid verification code or code expired");
        }

        // Prepare user info & token (without sensitive data)
        UserRegistrationResponseDTO responseDTO = authService.registerAndCreateResponse(registrationDTO);

        return ApiResponse.success("User registered successfully", responseDTO);
    }

    /**
     * Login a user
     * @param loginRequest
     * @return
     */
    @PostMapping("/login")
    public ApiResponse<UserRegistrationResponseDTO> login(@Valid @RequestBody UserLoginRequestDTO loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        UserRegistrationResponseDTO responseDTO = authService.loginAndCreateResponse(email, password);
        return ApiResponse.success("Login successful", responseDTO);
    }

    /**
     * Logout a user
     * @return
     */
    @PostMapping("/logout")
    public ApiResponse<String> logout() {
        // Clear SecurityContext
        SecurityContextHolder.clearContext();

        return ApiResponse.success("JWT tokens are stateless and cannot be invalidated server-side. Client should discard tokens.");
    }

    /**
     * Refresh a user's access token
     * @param refreshRequest
     * @return
     */
    @PostMapping("/refresh")
    public ApiResponse<UserRegistrationResponseDTO> refresh(@Valid @RequestBody RefreshRequestDTO refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();

        UserRegistrationResponseDTO responseDTO = authService.refreshToken(refreshToken);
        return ApiResponse.success("Token refreshed successfully", responseDTO);
    }

    /**
     * (re)Send verification email to a user
     * @param emailRequest
     * @return
     */
    @PostMapping("/send-email")
    public ApiResponse<String> sendEmail(@RequestBody EmailVerificationRequestDTO emailRequest) {
        String email = emailRequest.getEmail();

        //query email if exists
        User user = userMapper.selectByEmail(email);
        if (user != null) {
            throw new ValidationException("Email already exists");
        }

        mailService.sendVerificationCode(email);

        return ApiResponse.success("Verification code sent successfully");
    }
}
