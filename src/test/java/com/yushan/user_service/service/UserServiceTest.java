package com.yushan.user_service.service;

import com.yushan.user_service.dao.UserMapper;
import com.yushan.user_service.dto.UserProfileResponseDTO;
import com.yushan.user_service.dto.UserProfileUpdateRequestDTO;
import com.yushan.user_service.dto.UserProfileUpdateResponseDTO;
import com.yushan.user_service.entity.User;
import com.yushan.user_service.enums.Gender;
import com.yushan.user_service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserMapper userMapper;
    private MailService mailService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userMapper = Mockito.mock(UserMapper.class);
        mailService = Mockito.mock(MailService.class);
        userService = new UserService();

        // Inject mock mapper via reflection (simple without Spring context)
        try {
            java.lang.reflect.Field f = UserService.class.getDeclaredField("userMapper");
            f.setAccessible(true);
            f.set(userService, userMapper);

            java.lang.reflect.Field f2 = UserService.class.getDeclaredField("mailService");
            f2.setAccessible(true);
            f2.set(userService, mailService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getUserProfile_returnsNull_whenUserNotFound() {
        UUID id = UUID.randomUUID();
        when(userMapper.selectByPrimaryKey(id)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserProfile(id));
        verify(userMapper).selectByPrimaryKey(id);
    }

    @Test
    void updateUserProfileSelective_updatesOnlyProvidedFields_andReturnsDto() {
        UUID id = UUID.randomUUID();

        User existing = new User();
        existing.setUuid(id);
        existing.setEmail("old@example.com");
        existing.setUsername("oldname");
        existing.setAvatarUrl("old.png");
        existing.setProfileDetail("old profile");
        existing.setGender(1);
        existing.setLastLogin(new Date());
        existing.setLastActive(new Date());

        when(userMapper.selectByPrimaryKey(id)).thenReturn(existing);

        User after = new User();
        after.setUuid(id);
        after.setEmail("old@example.com");
        after.setUsername("newname");
        after.setAvatarUrl("new.png");
        after.setProfileDetail("new profile");
        after.setGender(2);
        after.setLastLogin(new Date());
        after.setLastActive(new Date());
        after.setUpdateTime(new Date());

        when(userMapper.selectByPrimaryKey(id)).thenReturn(existing, after);

        UserProfileUpdateRequestDTO req = new UserProfileUpdateRequestDTO();
        req.setUsername("newname");
        req.setAvatarBase64("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");
        req.setProfileDetail("new profile");
        req.setGender(Gender.FEMALE);

        UserProfileUpdateResponseDTO dto = userService.updateUserProfileSelective(id, req);

        // verify mapper called with selective update
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateByPrimaryKeySelective(captor.capture());
        User updatedArg = captor.getValue();
        assertEquals(id, updatedArg.getUuid());
        assertEquals("newname", updatedArg.getUsername());
        assertEquals("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==", updatedArg.getAvatarUrl());
        assertEquals("new profile", updatedArg.getProfileDetail());
        assertEquals(2, updatedArg.getGender());
        assertNotNull(updatedArg.getUpdateTime());

        // validate returned DTO fields
        assertNotNull(dto);
        assertNotNull(dto.getProfile());
        assertEquals(id.toString(), dto.getProfile().getUuid());
        assertEquals("newname", dto.getProfile().getUsername());
        assertEquals("new.png", dto.getProfile().getAvatarUrl());
        assertEquals("new profile", dto.getProfile().getProfileDetail());
        assertEquals(Gender.FEMALE, dto.getProfile().getGender());
        assertFalse(dto.isEmailChanged());
    }

    @Test
    void updateUserProfileSelective_changeEmail_withoutCode_throws() {
        UUID id = UUID.randomUUID();
        User existing = new User();
        existing.setUuid(id);
        existing.setEmail("old@example.com");
        existing.setAvatarUrl("https://example.com/avatar.jpg");
        existing.setGender(1);
        existing.setLastLogin(new Date());
        existing.setLastActive(new Date());
        when(userMapper.selectByPrimaryKey(id)).thenReturn(existing);

        UserProfileUpdateRequestDTO req = new UserProfileUpdateRequestDTO();
        req.setEmail("new@example.com"); // no code provided

        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUserProfileSelective(id, req));
    }

    @Test
    void updateUserProfileSelective_changeEmail_withInvalidCode_throws() {
        UUID id = UUID.randomUUID();
        User existing = new User();
        existing.setUuid(id);
        existing.setEmail("old@example.com");
        existing.setAvatarUrl("https://example.com/avatar.jpg");
        existing.setGender(1);
        existing.setLastLogin(new Date());
        existing.setLastActive(new Date());
        when(userMapper.selectByPrimaryKey(id)).thenReturn(existing);

        when(mailService.verifyEmail("new@example.com", "000000")).thenReturn(false);

        UserProfileUpdateRequestDTO req = new UserProfileUpdateRequestDTO();
        req.setEmail("new@example.com");
        req.setVerificationCode("000000");

        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUserProfileSelective(id, req));
    }

    @Test
    void updateUserProfileSelective_changeEmail_success_updatesEmail() {
        UUID id = UUID.randomUUID();
        User existing = new User();
        existing.setUuid(id);
        existing.setEmail("old@example.com");
        existing.setAvatarUrl("https://example.com/avatar.jpg");
        existing.setGender(1);
        existing.setLastLogin(new Date());
        existing.setLastActive(new Date());

        User after = new User();
        after.setUuid(id);
        after.setEmail("new@example.com");
        after.setUsername("same");
        after.setAvatarUrl("https://example.com/avatar.jpg");
        after.setGender(1);
        after.setLastLogin(new Date());
        after.setLastActive(new Date());

        when(userMapper.selectByPrimaryKey(id)).thenReturn(existing, after);
        when(userMapper.selectByEmail("new@example.com")).thenReturn(null);
        when(mailService.verifyEmail("new@example.com", "123456")).thenReturn(true);

        UserProfileUpdateRequestDTO req = new UserProfileUpdateRequestDTO();
        req.setEmail("new@example.com");
        req.setVerificationCode("123456");

        UserProfileUpdateResponseDTO dto = userService.updateUserProfileSelective(id, req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateByPrimaryKeySelective(captor.capture());
        assertEquals("new@example.com", captor.getValue().getEmail());
        assertEquals("new@example.com", dto.getProfile().getEmail());
        assertTrue(dto.isEmailChanged());
    }

    @Test
    void sendEmailChangeVerification_emailExists_throws() {
        when(userMapper.selectByEmail("dup@example.com")).thenReturn(new User());
        assertThrows(IllegalArgumentException.class,
                () -> userService.sendEmailChangeVerification("dup@example.com"));
        verify(mailService, never()).sendVerificationCode(anyString());
    }

    @Test
    void sendEmailChangeVerification_success_callsMailService() {
        when(userMapper.selectByEmail("free@example.com")).thenReturn(null);
        userService.sendEmailChangeVerification("free@example.com");
        verify(mailService).sendVerificationCode("free@example.com");
    }

    @Test
    void getUserProfile_returnsIsAdminField_forAdminUser() {
        UUID id = UUID.randomUUID();
        User adminUser = new User();
        adminUser.setUuid(id);
        adminUser.setEmail("admin@example.com");
        adminUser.setUsername("AdminUser");
        adminUser.setAvatarUrl("https://example.com/admin-avatar.jpg");
        adminUser.setGender(1);
        adminUser.setLastLogin(new Date());
        adminUser.setLastActive(new Date());
        adminUser.setIsAuthor(true);
        adminUser.setIsAdmin(true);  // Admin user

        when(userMapper.selectByPrimaryKey(id)).thenReturn(adminUser);

        var profile = userService.getUserProfile(id);

        assertNotNull(profile);
        assertEquals(id.toString(), profile.getUuid());
        assertEquals("admin@example.com", profile.getEmail());
        assertEquals("AdminUser", profile.getUsername());
        assertTrue(profile.getIsAuthor());
        assertTrue(profile.getIsAdmin());  // Should be true for admin
    }

    @Test
    void getAllUsers_returnsListOfUserProfiles() {
        // Arrange
        User user1 = new User();
        user1.setUuid(UUID.randomUUID());
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setGender(1);
        user1.setStatus(0);

        User user2 = new User();
        user2.setUuid(UUID.randomUUID());
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setGender(2);
        user2.setStatus(0);

        List<User> userList = List.of(user1, user2);
        when(userMapper.selectAllUsersForRanking()).thenReturn(userList);

        // Act
        List<UserProfileResponseDTO> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(user1.getUuid().toString(), result.get(0).getUuid());
        assertEquals(user1.getUsername(), result.get(0).getUsername());
        assertEquals(user2.getUuid().toString(), result.get(1).getUuid());
        assertEquals(user2.getUsername(), result.get(1).getUsername());

        verify(userMapper).selectAllUsersForRanking();
    }

    @Test
    void getUsersByIds_returnsMatchingUserProfiles() {
        // Arrange
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<UUID> userIds = List.of(id1, id2);

        User user1 = new User();
        user1.setUuid(id1);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setGender(1);
        user1.setStatus(0);

        User user2 = new User();
        user2.setUuid(id2);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setGender(2);
        user2.setStatus(0);

        List<User> userList = List.of(user1, user2);
        when(userMapper.selectByUuids(userIds)).thenReturn(userList);

        // Act
        List<UserProfileResponseDTO> result = userService.getUsersByIds(userIds);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(id1.toString(), result.get(0).getUuid());
        assertEquals(id2.toString(), result.get(1).getUuid());

        verify(userMapper).selectByUuids(userIds);
    }

    @Test
    void getUsersByIds_withEmptyList_returnsEmptyList() {
        // Arrange
        List<UUID> emptyIdList = List.of();
        when(userMapper.selectByUuids(emptyIdList)).thenReturn(List.of());

        // Act
        List<UserProfileResponseDTO> result = userService.getUsersByIds(emptyIdList);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userMapper).selectByUuids(emptyIdList);
    }

    @Test
    void getUserProfile_returnsIsAdminField_forNormalUser() {
        UUID id = UUID.randomUUID();
        User normalUser = new User();
        normalUser.setUuid(id);
        normalUser.setEmail("normal@example.com");
        normalUser.setUsername("NormalUser");
        normalUser.setAvatarUrl("https://example.com/normal-avatar.jpg");
        normalUser.setGender(1);
        normalUser.setLastLogin(new Date());
        normalUser.setLastActive(new Date());
        normalUser.setIsAuthor(false);
        normalUser.setIsAdmin(false);  // Normal user

        when(userMapper.selectByPrimaryKey(id)).thenReturn(normalUser);

        var profile = userService.getUserProfile(id);

        assertNotNull(profile);
        assertEquals(id.toString(), profile.getUuid());
        assertEquals("normal@example.com", profile.getEmail());
        assertEquals("NormalUser", profile.getUsername());
        assertFalse(profile.getIsAuthor());
        assertFalse(profile.getIsAdmin());  // Should be false for normal user
    }

}


