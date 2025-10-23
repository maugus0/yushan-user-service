package com.yushan.user_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yushan.user_service.TestcontainersConfiguration;
import com.yushan.user_service.client.ContentServiceClient;
import com.yushan.user_service.client.dto.ChapterInfoDTO;
import com.yushan.user_service.client.dto.NovelInfoDTO;
import com.yushan.user_service.dao.LibraryMapper;
import com.yushan.user_service.dao.NovelLibraryMapper;
import com.yushan.user_service.dao.UserMapper;
import com.yushan.user_service.entity.Library;
import com.yushan.user_service.entity.NovelLibrary;
import com.yushan.user_service.entity.User;
import com.yushan.user_service.enums.ErrorCode;
import com.yushan.user_service.enums.Gender;
import com.yushan.user_service.enums.NovelStatus;
import com.yushan.user_service.enums.UserStatus;
import com.yushan.user_service.service.MailService;
import com.yushan.user_service.util.JwtUtil;
import com.yushan.user_service.dto.ApiResponse;
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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for LibraryController with real PostgreSQL + Redis
 *
 * This test class verifies:
 * - Adding, removing, and listing novels in a user's library
 * - Batch removal of novels
 * - Checking for a novel's existence in the library
 * - Updating and retrieving reading progress
 * - Database persistence for all library operations
 */
@SpringBootTest
@ActiveProfiles("integration-test")
@Import(TestcontainersConfiguration.class)
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
public class LibraryIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LibraryMapper libraryMapper;

    @Autowired
    private NovelLibraryMapper novelLibraryMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContentServiceClient contentServiceClient;

    @MockBean
    private MailService mailService;
    @MockBean
    private MailUtil mailUtil;

    private MockMvc mockMvc;

    private User testUser;
    private String testUserToken;
    private Library testUserLibrary;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Mock the Feign client to prevent network errors
        NovelInfoDTO mockNovel = new NovelInfoDTO(1, "Test Novel", "Test Author", "cover.jpg", 100, NovelStatus.PUBLISHED.name());
        when(contentServiceClient.getNovelById(anyInt())).thenReturn(ApiResponse.success("Success", mockNovel));

        // Create a test user
        testUser = createTestUser("libraryuser@example.com", "libraryuser", "password123");
        userMapper.insert(testUser);

        // Create a library for the test user for tests that require it to exist beforehand
        testUserLibrary = new Library();
        testUserLibrary.setUuid(UUID.randomUUID());
        testUserLibrary.setUserId(testUser.getUuid());
        testUserLibrary.setCreateTime(new Date());
        testUserLibrary.setUpdateTime(new Date());
        libraryMapper.insert(testUserLibrary);

        testUserToken = jwtUtil.generateAccessToken(testUser);
    }

    @Test
    void testAddNovelToLibrary_Success() throws Exception {
        Integer novelId = 1;

        mockMvc.perform(post("/api/v1/library/{novelId}", novelId)
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // Send empty JSON body
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value("Add novel to library successfully"));

        // Verify that the novel is in the novel_library table, linked to the correct user
        NovelLibrary libraryItem = novelLibraryMapper.selectByUserIdAndNovelId(testUser.getUuid(), novelId);
        assertThat(libraryItem).isNotNull();
        assertThat(libraryItem.getNovelId()).isEqualTo(novelId);
    }

    @Test
    void testRemoveNovelFromLibrary_Success() throws Exception {
        Integer novelId = 2;
        addNovelToDb(testUserLibrary.getId(), novelId, 0);

        mockMvc.perform(delete("/api/v1/library/{novelId}", novelId)
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value("Remove novel from library successfully"));

        NovelLibrary libraryItem = novelLibraryMapper.selectByUserIdAndNovelId(testUser.getUuid(), novelId);
        assertThat(libraryItem).isNull();
    }

    @Test
    void testBatchRemoveNovelsFromLibrary_Success() throws Exception {
        Integer novelId1 = 3;
        Integer novelId2 = 4;
        addNovelToDb(testUserLibrary.getId(), novelId1, 0);
        addNovelToDb(testUserLibrary.getId(), novelId2, 0);

        Map<String, List<Integer>> request = new HashMap<>();
        request.put("ids", Arrays.asList(novelId1, novelId2));

        mockMvc.perform(delete("/api/v1/library/batch")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value("batch remove successfully"));

        assertThat(novelLibraryMapper.selectByUserIdAndNovelId(testUser.getUuid(), novelId1)).isNull();
        assertThat(novelLibraryMapper.selectByUserIdAndNovelId(testUser.getUuid(), novelId2)).isNull();
    }

    @Test
    void testGetUserLibrary_Success() throws Exception {
        addNovelToDb(testUserLibrary.getId(), 5, 10);
        addNovelToDb(testUserLibrary.getId(), 6, 20);

        // Mock the Feign client for this specific test
        List<NovelInfoDTO> mockNovels = Arrays.asList(
                new NovelInfoDTO(5, "Novel 5", "Author 5", "c5.jpg",  10, NovelStatus.PUBLISHED.name()),
                new NovelInfoDTO(6, "Novel 6", "Author 6", "c6.jpg",  20, NovelStatus.PUBLISHED.name())
        );
        when(contentServiceClient.getNovelsByIds(Arrays.asList(5, 6))).thenReturn(ApiResponse.success("Success", mockNovels));
        when(contentServiceClient.getChaptersByIds(any())).thenReturn(ApiResponse.success("Success", Collections.emptyList()));


        mockMvc.perform(get("/api/v1/library")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    void testCheckNovelInLibrary_ReturnsTrueWhenInLibrary() throws Exception {
        Integer novelId = 7;
        addNovelToDb(testUserLibrary.getId(), novelId, 0);

        mockMvc.perform(get("/api/v1/library/check/{novelId}", novelId)
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testCheckNovelInLibrary_ReturnsFalseWhenNotInLibrary() throws Exception {
        Integer novelId = 8;
        mockMvc.perform(get("/api/v1/library/check/{novelId}", novelId)
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    void testGetLibraryNovel_Success() throws Exception {
        Integer novelId = 9;
        addNovelToDb(testUserLibrary.getId(), novelId, 50);

        // Mock the Feign client for this specific test
        List<ChapterInfoDTO> mockChapter = Arrays.asList(
                new ChapterInfoDTO(50, 1, 9)
        );

        when(contentServiceClient.getChaptersByIds(any())).thenReturn(ApiResponse.success("Success", mockChapter));

        mockMvc.perform(get("/api/v1/library/{novelId}", novelId)
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.novelId").value(novelId))
                .andExpect(jsonPath("$.data.progress").value(50));
    }

    @Test
    void testUpdateReadingProgress_Success() throws Exception {
        Integer novelId = 10;
        addNovelToDb(testUserLibrary.getId(), novelId, 0);

        Map<String, Integer> request = new HashMap<>();
        request.put("progress", 75);
        // Mock the Feign client for this specific test
        List<ChapterInfoDTO> mockChapter = Arrays.asList(
                new ChapterInfoDTO(75, 1, 10)
        );

        when(contentServiceClient.getChaptersByIds(any())).thenReturn(ApiResponse.success("Success", mockChapter));

        mockMvc.perform(patch("/api/v1/library/{novelId}/progress", novelId)
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.progress").value(75));

        NovelLibrary libraryItem = novelLibraryMapper.selectByUserIdAndNovelId(testUser.getUuid(), novelId);
        assertThat(libraryItem.getProgress()).isEqualTo(75);
    }

    private User createTestUser(String email, String username, String password) {
        User user = new User();
        user.setUuid(UUID.randomUUID());
        user.setEmail(email);
        user.setUsername(username);
        user.setHashPassword(passwordEncoder.encode(password));
        user.setAvatarUrl("https://example.com/avatar.jpg");
        user.setStatus(UserStatus.NORMAL.getCode());
        user.setGender(Gender.MALE.getCode());
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setLastLogin(new Date());
        user.setLastActive(new Date());
        user.setIsAuthor(false);
        user.setIsAdmin(false);
        return user;
    }

    private void addNovelToDb(Integer libraryId, Integer novelId, Integer progress) {
        NovelLibrary item = new NovelLibrary();
        item.setLibraryId(libraryId);
        item.setNovelId(novelId);
        item.setProgress(progress);
        item.setCreateTime(new Date());
        item.setUpdateTime(new Date());
        novelLibraryMapper.insert(item);
    }
}
