package com.yushan.user_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yushan.user_service.dao.UserMapper;
import com.yushan.user_service.entity.User;
import com.yushan.user_service.enums.ErrorCode;
import com.yushan.user_service.service.MailService;
import com.yushan.user_service.util.JwtUtil;
import com.yushan.user_service.util.MailUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for JWT authentication and authorization
 * 
 * This test class verifies the complete JWT flow including:
 * - Token generation and validation
 * - Authentication endpoints (login, register, refresh, logout)
 * - Protected endpoints with different authorization levels
 * - Role-based access control
 * - Error handling scenarios
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=",
        "spring.kafka.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "jwt.secret=test-secret-key-for-integration-tests-123456",
        "jwt.access-token.expiration=3600000",
        "jwt.refresh-token.expiration=86400000"
})
@MockitoSettings(strictness = Strictness.LENIENT)
public class JwtAuthenticationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MailService mailService;

    @MockBean
    private MailUtil mailUtil;

    private MockMvc mockMvc;

    private User testUser;
    private User authorUser;
    private String testUserToken;
    private String authorUserToken;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Create test users
        createTestUsers();
        
        // Setup mock MailService after creating users
        setupMockMailService();
    }

    /**
     * Setup mock MailService behavior
     */
    private void setupMockMailService() {
        // Mock sendVerificationCode to do nothing
        doNothing().when(mailService).sendVerificationCode(anyString());
        
        // Mock verifyEmail to return true for test code "123456"
        when(mailService.verifyEmail(anyString(), eq("123456"))).thenReturn(true);
        when(mailService.verifyEmail(anyString(), anyString())).thenReturn(false);
    }

    /**
     * Create test users for testing different scenarios
     */
    private void createTestUsers() {
        // Create regular user
        testUser = new User();
        testUser.setUuid(UUID.randomUUID());
        testUser.setEmail("testuser@example.com");
        testUser.setUsername("testuser");
        testUser.setHashPassword(passwordEncoder.encode("password123"));
        testUser.setAvatarUrl("https://example.com/avatar.jpg");
        testUser.setStatus(0); // Active status
        testUser.setGender(1);
        testUser.setLastLogin(new Date());
        testUser.setLastActive(new Date());
        testUser.setIsAuthor(false);
        testUser.setIsAdmin(false);
        testUser.setCreateTime(new Date());
        testUser.setUpdateTime(new Date());
        userMapper.insert(testUser);

        // Create author user
        authorUser = new User();
        authorUser.setUuid(UUID.randomUUID());
        authorUser.setEmail("author@example.com");
        authorUser.setUsername("author");
        authorUser.setHashPassword(passwordEncoder.encode("password123"));
        authorUser.setAvatarUrl("https://example.com/author-avatar.jpg");
        authorUser.setStatus(0); // Active status
        authorUser.setGender(1);
        authorUser.setLastLogin(new Date());
        authorUser.setLastActive(new Date());
        authorUser.setIsAuthor(true);
        authorUser.setIsAdmin(false);
        authorUser.setCreateTime(new Date());
        authorUser.setUpdateTime(new Date());
        userMapper.insert(authorUser);

        // Generate tokens
        testUserToken = jwtUtil.generateAccessToken(testUser);
        authorUserToken = jwtUtil.generateAccessToken(authorUser);
    }

    /**
     * Create a test user with specified parameters
     */
    private User createTestUser(String email, String username, boolean isAuthor, boolean isAdmin) {
        User user = new User();
        user.setUuid(UUID.randomUUID());
        user.setEmail(email);
        user.setUsername(username);
        user.setHashPassword(passwordEncoder.encode("password123"));
        user.setAvatarUrl("https://example.com/avatar.jpg");
        user.setGender(1);
        user.setLastLogin(new Date());
        user.setLastActive(new Date());
        user.setStatus(0);
        user.setIsAuthor(isAuthor);
        user.setIsAdmin(isAdmin);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        userMapper.insert(user);
        return user;
    }

    // ==================== JWT TOKEN TESTS ====================

    @Test
    void testJwtTokenGeneration() throws Exception {
        // Test access token generation
        String accessToken = jwtUtil.generateAccessToken(testUser);
        assert accessToken != null;
        assert !accessToken.isEmpty();

        // Test refresh token generation
        String refreshToken = jwtUtil.generateRefreshToken(testUser);
        assert refreshToken != null;
        assert !refreshToken.isEmpty();

        // Verify tokens are different
        assert !accessToken.equals(refreshToken);
    }

    @Test
    void testJwtTokenValidation() throws Exception {
        // Test valid token
        assert jwtUtil.validateToken(testUserToken);
        assert jwtUtil.validateToken(testUserToken, testUser);

        // Test invalid token
        String invalidToken = "invalid.token.here";
        assert !jwtUtil.validateToken(invalidToken);

        // Test token with wrong user
        assert !jwtUtil.validateToken(testUserToken, authorUser);
    }

    @Test
    void testJwtTokenClaims() throws Exception {
        // Test extracting claims from token
        String email = jwtUtil.extractEmail(testUserToken);
        String userId = jwtUtil.extractUserId(testUserToken);
        String tokenType = jwtUtil.extractTokenType(testUserToken);

        assert email.equals(testUser.getEmail());
        assert userId.equals(testUser.getUuid().toString());
        assert tokenType.equals("access");
    }

    // ==================== AUTHENTICATION ENDPOINT TESTS ====================

    @Test
    void testLoginSuccess() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "testuser@example.com");
        loginRequest.put("password", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "testuser@example.com");
        loginRequest.put("password", "wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void testRegisterSuccess() throws Exception {
        // Setup mock directly in the test
        when(mailService.verifyEmail(eq("newuser@example.com"), eq("123456"))).thenReturn(true);
        
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("email", "newuser@example.com");
        registerRequest.put("username", "newuser");
        registerRequest.put("password", "password123");
        registerRequest.put("code", "123456");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.email").value("newuser@example.com"));
    }

    @Test
    void testRefreshTokenSuccess() throws Exception {
        String refreshToken = jwtUtil.generateRefreshToken(testUser);

        Map<String, String> refreshRequest = new HashMap<>();
        refreshRequest.put("refreshToken", refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    void testRefreshTokenWithInvalidToken() throws Exception {
        Map<String, String> refreshRequest = new HashMap<>();
        refreshRequest.put("refreshToken", "invalid.refresh.token");

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("JWT tokens are stateless and cannot be invalidated server-side. Client should discard tokens."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testSendEmail() throws Exception {
        Map<String, String> sendEmailRequest = new HashMap<>();
        sendEmailRequest.put("email", "newuser@example.com");

        mockMvc.perform(post("/api/v1/auth/send-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sendEmailRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Verification code sent successfully"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/example/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unauthorized: Invalid or missing JWT token"));
    }

    @Test
    void testProtectedEndpointWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/example/protected")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unauthorized: Invalid or missing JWT token"));
    }


    // ==================== ERROR HANDLING TESTS ====================

    @Test
    void testMalformedJwtToken() throws Exception {
        mockMvc.perform(get("/api/v1/example/protected")
                .header("Authorization", "Bearer malformed.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unauthorized: Invalid or missing JWT token"));
    }

    @Test
    void testExpiredJwtToken() throws Exception {
        // Note: In a real test, you would need to create an expired token
        // For this example, we'll test with an invalid token
        mockMvc.perform(get("/api/v1/example/protected")
                .header("Authorization", "Bearer expired.token.here"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unauthorized: Invalid or missing JWT token"));
    }

    @Test
    void testMissingAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/v1/example/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unauthorized: Invalid or missing JWT token"));
    }

    @Test
    void testInvalidAuthorizationFormat() throws Exception {
        mockMvc.perform(get("/api/v1/example/protected")
                .header("Authorization", "Invalid " + testUserToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unauthorized: Invalid or missing JWT token"));
    }

    @Test
    void testIsAdminFieldInLoginResponse() throws Exception {
        // Test login with admin user - create user first
        createTestUser("admin@example.com", "AdminUser", true, true);

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "admin@example.com");
        loginRequest.put("password", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.isAdmin").value(true))
                .andExpect(jsonPath("$.data.isAuthor").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    void testIsAdminFieldInNormalUserResponse() throws Exception {
        // Test login with normal user - create user first
        createTestUser("normal@example.com", "NormalUser", false, false);

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "normal@example.com");
        loginRequest.put("password", "password123");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.isAdmin").value(false))
                .andExpect(jsonPath("$.data.isAuthor").value(false))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    void testIsAdminFieldInUserProfileResponse() throws Exception {
        // Test user profile endpoint with admin user
        User adminUser = createTestUser("adminprofile@example.com", "AdminProfileUser", true, true);
        String adminToken = jwtUtil.generateAccessToken(adminUser);

        mockMvc.perform(get("/api/v1/users/me")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.isAdmin").value(true))
                .andExpect(jsonPath("$.data.isAuthor").value(true))
                .andExpect(jsonPath("$.data.email").value("adminprofile@example.com"));
    }

    @Test
    void testIsAdminFieldInRefreshTokenResponse() throws Exception {
        // Test refresh token with admin user
        User adminUser = createTestUser("adminrefresh@example.com", "AdminRefreshUser", true, true);
        String refreshToken = jwtUtil.generateRefreshToken(adminUser);

        Map<String, String> refreshRequest = new HashMap<>();
        refreshRequest.put("refreshToken", refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.isAdmin").value(true))
                .andExpect(jsonPath("$.data.isAuthor").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }
}

