package com.yushan.user_service.service;

import com.yushan.user_service.dao.UserMapper;
import com.yushan.user_service.dto.AdminUserFilterDTO;
import com.yushan.user_service.dto.PageResponseDTO;
import com.yushan.user_service.dto.UserProfileResponseDTO;
import com.yushan.user_service.entity.User;
import com.yushan.user_service.enums.UserStatus;
import com.yushan.user_service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AdminService adminService;

    private User testUser;
    private UUID testUserUuid;

    @BeforeEach
    void setUp() {
        testUserUuid = UUID.randomUUID();
        testUser = new User();
        testUser.setUuid(testUserUuid);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
    }

    @Nested
    @DisplayName("listUsers Tests")
    class ListUsers {
        @Test
        @DisplayName("Should return a paginated list of users")
        void shouldReturnPaginatedUsers() {
            // Given
            AdminUserFilterDTO filter = new AdminUserFilterDTO();
            filter.setPage(0);
            filter.setSize(10);
            int offset = 0;
            List<User> users = Collections.singletonList(testUser);

            when(userMapper.countUsersForAdmin(filter)).thenReturn(1L);
            when(userMapper.selectUsersForAdmin(filter, offset)).thenReturn(users);

            // When
            PageResponseDTO<UserProfileResponseDTO> result = adminService.listUsers(filter);

            // Then
            assertEquals(1, result.getTotalElements());
            assertEquals(1, result.getContent().size());
            assertEquals(testUser.getUsername(), result.getContent().get(0).getUsername());
            verify(userMapper).countUsersForAdmin(filter);
            verify(userMapper).selectUsersForAdmin(filter, offset);
        }
    }

    @Nested
    @DisplayName("updateUserStatus Tests")
    class UpdateUserStatus {
        @Test
        @DisplayName("Should update user status successfully")
        void shouldUpdateStatus() {
            // Given
            when(userMapper.selectByPrimaryKey(testUserUuid)).thenReturn(testUser);

            // When
            adminService.updateUserStatus(testUserUuid, UserStatus.BANNED);

            // Then
            verify(userMapper).updateByPrimaryKeySelective(argThat(user ->
                    user.getUuid().equals(testUserUuid) &&
                            user.getStatus().equals(UserStatus.BANNED.ordinal())
            ));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user to update is not found")
        void shouldThrowWhenUpdatingNonExistentUser() {
            // Given
            when(userMapper.selectByPrimaryKey(testUserUuid)).thenReturn(null);

            // When & Then
            assertThrows(ResourceNotFoundException.class, () ->
                    adminService.updateUserStatus(testUserUuid, UserStatus.BANNED));
            verify(userMapper, never()).updateByPrimaryKeySelective(any());
        }
    }
}