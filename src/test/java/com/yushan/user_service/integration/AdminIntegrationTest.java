package com.yushan.user_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yushan.user_service.TestcontainersConfiguration;
import com.yushan.user_service.config.DatabaseConfig;
import com.yushan.user_service.dao.UserMapper;
import com.yushan.user_service.dto.AdminUpdateUserDTO;
import com.yushan.user_service.entity.User;
import com.yushan.user_service.enums.Gender;
import com.yushan.user_service.enums.UserStatus;
import com.yushan.user_service.event.UserActivityEventProducer;
import com.yushan.user_service.service.MailService;
import com.yushan.user_service.service.UserService;
import com.yushan.user_service.util.JwtUtil;
import com.yushan.user_service.util.MailUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("integration-test")
@Import({TestcontainersConfiguration.class, DatabaseConfig.class})
@Transactional
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=",
        "spring.kafka.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "jwt.secret=test-secret-key-for-integration-tests-123456",
        "jwt.access-token.expiration=3600000",
        "jwt.refresh-token.expiration=86400000"
})
@org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named = "CI", matches = "true")
public class AdminIntegrationTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private MailService mailService;
    @MockBean
    private MailUtil mailUtil;

    @MockBean
    private UserActivityEventProducer userActivityEventProducer;

    private MockMvc mockMvc;

    private User adminUser;
    private User normalUser;
    private User authorUser;
    private String adminToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        createTestData();
    }

    @Test
    void testListUsers_withIsAuthorFilter_shouldReturnCorrectUsers() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users?isAuthor=true")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.username == 'admin_user')]").exists())
                .andExpect(jsonPath("$.data.content[?(@.username == 'author_user')]").exists());
    }

    @Test
    void testUpdateUser_shouldChangeStatusAndRoleInDatabase() throws Exception {
        // Given
        AdminUpdateUserDTO requestBody = new AdminUpdateUserDTO();
        requestBody.setStatus(UserStatus.BANNED);

        // When
        mockMvc.perform(put("/api/v1/admin/users/{uuid}/status", normalUser.getUuid())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk());

        // Then: Verify changes in the database
        User updatedUser = userMapper.selectByPrimaryKey(normalUser.getUuid());
        assertThat(updatedUser.getStatus()).isEqualTo(UserStatus.BANNED.ordinal());
        assertThat(updatedUser.getIsAuthor()).isFalse();
        assertThat(updatedUser.getIsAdmin()).isFalse();
    }

    // Helper Methods
    private void createTestData() {
        adminUser = createTestUser("admin@test.com", "admin_user", true, true);
        authorUser = createTestUser("author@test.com", "author_user", true, false);
        normalUser = createTestUser("user@test.com", "normal_user", false, false);

        userMapper.insert(adminUser);
        userMapper.insert(authorUser);
        userMapper.insert(normalUser);

        adminToken = jwtUtil.generateAccessToken(adminUser);
    }

    private User createTestUser(String email, String username, boolean isAuthor, boolean isAdmin) {
        User user = new User();
        user.setUuid(UUID.randomUUID());
        user.setEmail(email);
        user.setUsername(username);
        user.setHashPassword(passwordEncoder.encode("password123"));
        user.setAvatarUrl("https://example.com/avatar.jpg");
        user.setStatus(UserStatus.NORMAL.getCode());
        user.setGender(Gender.MALE.getCode());
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setLastLogin(new Date());
        user.setLastActive(new Date());
        user.setIsAuthor(isAuthor);
        user.setIsAdmin(isAdmin);
        return user;
    }
}