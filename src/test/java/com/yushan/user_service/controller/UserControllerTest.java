package com.yushan.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yushan.user_service.dao.UserMapper;
import com.yushan.user_service.dto.EmailVerificationRequestDTO;
import com.yushan.user_service.dto.UserProfileResponseDTO;
import com.yushan.user_service.dto.UserProfileUpdateRequestDTO;
import com.yushan.user_service.dto.UserProfileUpdateResponseDTO;
import com.yushan.user_service.entity.User;
import com.yushan.user_service.event.UserActivityEventProducer;
import com.yushan.user_service.security.CustomUserDetailsService.CustomUserDetails;
import com.yushan.user_service.security.SecurityExpressionRoot;
import com.yushan.user_service.service.MailService;
import com.yushan.user_service.service.UserService;
import com.yushan.user_service.util.JwtUtil;
import com.yushan.user_service.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@EnableMethodSecurity
@ActiveProfiles("test")
public class UserControllerTest {

    @Retention(RetentionPolicy.RUNTIME)
    @WithSecurityContext(factory = WithCustomUserDetailsSecurityContextFactory.class)
    public @interface WithCustomUserDetails {
        String email() default "test@example.com";
        String userId() default "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11";
    }

    public static class WithCustomUserDetailsSecurityContextFactory implements WithSecurityContextFactory<WithCustomUserDetails> {
        @Override
        public SecurityContext createSecurityContext(WithCustomUserDetails annotation) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();

            // Mock User entity
            User user = new User();
            user.setUuid(UUID.fromString(annotation.userId()));
            user.setEmail(annotation.email());
            user.setUsername("testuser");
            user.setHashPassword("password");
            user.setAvatarUrl("https://example.com/avatar.jpg");
            user.setGender(1);
            user.setLastLogin(new Date());
            user.setLastActive(new Date());
            user.setIsAuthor(false);
            user.setIsAdmin(false);

            // Mock CustomUserDetails
            CustomUserDetails userDetails = new CustomUserDetails(user);

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            context.setAuthentication(auth);
            return context;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private MailService mailService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private RedisUtil redisUtil;

    @MockBean
    private UserActivityEventProducer userActivityEventProducer;

    private SecurityExpressionRoot securityExpressionRoot;
    private User testUser;
    private UUID testUserId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
    private String testEmail = "test@example.com";
    private UserProfileResponseDTO response;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUuid(testUserId);
        testUser.setEmail(testEmail);
        testUser.setUsername("testuser");
        testUser.setAvatarUrl("https://example.com/avatar.jpg");
        testUser.setGender(1);
        testUser.setLastLogin(new Date());
        testUser.setLastActive(new Date());
        testUser.setIsAuthor(false);
        testUser.setIsAdmin(false);

        response = new UserProfileResponseDTO();
        response.setEmail(testEmail);
        response.setUsername("testuser");
        response.setIsAdmin(false);
    }

    @Test
    @WithCustomUserDetails(userId = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    void getCurrentUserProfile_Success() throws Exception {
        // Given
        when(userService.getUserProfile(testUserId)).thenReturn(response);
        // When & Then
        mockMvc.perform(get("/api/v1/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void getCurrentUserProfile_Unauthorized() throws Exception {
        // Given
        SecurityContextHolder.clearContext();

        // When & Then
        mockMvc.perform(get("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomUserDetails(userId = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    void updateProfile_Success() throws Exception {
        // Given
        UserProfileUpdateRequestDTO updateRequest = new UserProfileUpdateRequestDTO();
        updateRequest.setUsername("new_username");

        UserProfileUpdateResponseDTO updateResponse = new UserProfileUpdateResponseDTO();
        updateResponse.setEmailChanged(false);

        when(userService.updateUserProfileSelective(eq(testUserId), any(UserProfileUpdateRequestDTO.class)))
                .thenReturn(updateResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/users/{id}/profile", testUserId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Profile updated successfully"));
    }

    @Test
    @WithCustomUserDetails(userId = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    void updateProfile_Forbidden() throws Exception {
        // Given
        UUID anotherUserId = UUID.randomUUID();
        UserProfileUpdateRequestDTO updateRequest = new UserProfileUpdateRequestDTO();
        updateRequest.setUsername("new_username");

        // When & Then
        mockMvc.perform(put("/api/v1/users/{id}/profile", anotherUserId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void sendEmailChangeVerification_Success() throws Exception {
        // Given
        EmailVerificationRequestDTO emailRequest = new EmailVerificationRequestDTO();
        emailRequest.setEmail("new.email@example.com");
        when(userMapper.selectByEmail(testEmail)).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/api/v1/users/send-email-change-verification")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Verification code sent successfully"));
    }

    @Test
    @WithMockUser
    void sendEmailChangeVerification_InvalidEmail() throws Exception {
        // Given
        EmailVerificationRequestDTO emailRequest = new EmailVerificationRequestDTO();
        emailRequest.setEmail("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/v1/users/send-email-change-verification")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Failed to send verification email: Invalid email format"));
    }

    @Test
    @WithMockUser
    void getUserDetail_Success() throws Exception {
        // Given
        UUID targetUserId = UUID.randomUUID();
        UserProfileResponseDTO targetUserProfile = new UserProfileResponseDTO();
        targetUserProfile.setUuid(targetUserId.toString());
        targetUserProfile.setUsername("targetUser");

        when(userService.getUserProfile(targetUserId)).thenReturn(targetUserProfile);

        // When & Then
        mockMvc.perform(get("/api/v1/users/{userId}", targetUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.uuid").value(targetUserId.toString()));
    }

    @Test
    @WithMockUser
    void getUserDetail_UserNotFound() throws Exception {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();
        when(userService.getUserProfile(nonExistentUserId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/v1/users/{userId}", nonExistentUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @WithMockUser
    void getAllUsersForRanking_Success() throws Exception {
        // Arrange
        UserProfileResponseDTO user1 = new UserProfileResponseDTO();
        user1.setUuid(UUID.randomUUID().toString());
        user1.setUsername("user1");

        UserProfileResponseDTO user2 = new UserProfileResponseDTO();
        user2.setUuid(UUID.randomUUID().toString());
        user2.setUsername("user2");

        List<UserProfileResponseDTO> userList = List.of(user1, user2);
        when(userService.getAllUsers()).thenReturn(userList);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/all/ranking")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("User ranking retrieved successfully"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].username").value("user1"))
                .andExpect(jsonPath("$.data[1].username").value("user2"));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser
    void getUsersBatch_Success() throws Exception {
        // Arrange
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<UUID> requestIds = List.of(id1, id2);

        UserProfileResponseDTO user1 = new UserProfileResponseDTO();
        user1.setUuid(id1.toString());
        user1.setUsername("user1");

        UserProfileResponseDTO user2 = new UserProfileResponseDTO();
        user2.setUuid(id2.toString());
        user2.setUsername("user2");

        List<UserProfileResponseDTO> responseList = List.of(user1, user2);
        when(userService.getUsersByIds(requestIds)).thenReturn(responseList);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/batch/get")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("User profiles retrieved successfully"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].uuid").value(id1.toString()))
                .andExpect(jsonPath("$.data[1].uuid").value(id2.toString()));

        verify(userService).getUsersByIds(requestIds);
    }

    @Test
    @WithMockUser
    void getUsersBatch_WithEmptyList_ReturnsEmptyList() throws Exception {
        // Arrange
        List<UUID> requestIds = List.of();
        when(userService.getUsersByIds(requestIds)).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/batch/get")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(userService).getUsersByIds(requestIds);
    }

}
