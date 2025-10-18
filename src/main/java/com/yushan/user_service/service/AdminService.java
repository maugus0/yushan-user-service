package com.yushan.user_service.service;

import com.yushan.user_service.dao.UserMapper;
import com.yushan.user_service.dto.AdminUserFilterDTO;
import com.yushan.user_service.dto.PageResponseDTO;
import com.yushan.user_service.dto.UserProfileResponseDTO;
import com.yushan.user_service.entity.User;
import com.yushan.user_service.enums.Gender;
import com.yushan.user_service.enums.UserStatus;
import com.yushan.user_service.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    /**
     * Promote user to admin by email
     */
    public UserProfileResponseDTO promoteToAdmin(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        // Find user by email
        User user = userMapper.selectByEmail(email.trim().toLowerCase(java.util.Locale.ROOT));
        if (user == null) {
            throw new IllegalArgumentException("User not found with email: " + email);
        }

        // Check if user is already admin
        if (user.getIsAdmin() != null && user.getIsAdmin()) {
            throw new IllegalArgumentException("User is already an admin");
        }

        // Update user to admin
        user.setIsAdmin(true);
        user.setUpdateTime(new Date());
        userMapper.updateByPrimaryKeySelective(user);

        // Return updated user profile
        return userService.getUserProfile(user.getUuid());
    }

    public PageResponseDTO<UserProfileResponseDTO> listUsers(AdminUserFilterDTO filter) {
        int offset = filter.getPage() * filter.getSize();
        long totalElements = userMapper.countUsersForAdmin(filter);

        List<User> users = userMapper.selectUsersForAdmin(filter, offset);

        List<UserProfileResponseDTO> userProfiles = users.stream()
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());

        return new PageResponseDTO<>(userProfiles, totalElements, filter.getPage(), filter.getSize());
    }

    public void updateUserStatus(UUID userUuid, UserStatus newStatus) {
        User user = userMapper.selectByPrimaryKey(userUuid);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with UUID: " + userUuid);
        }

        User userToUpdate = new User();
        userToUpdate.setUuid(userUuid);
        userToUpdate.setStatus(newStatus.getCode());
        userToUpdate.setUpdateTime(new Date());

        userMapper.updateByPrimaryKeySelective(userToUpdate);
    }

    private UserProfileResponseDTO mapToProfileResponse(User user) {
        UserProfileResponseDTO dto = new UserProfileResponseDTO();
        dto.setUuid(user.getUuid() != null ? user.getUuid().toString() : null);
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setProfileDetail(user.getProfileDetail());
        dto.setBirthday(user.getBirthday());
        dto.setGender(Gender.fromCode(user.getGender()));
        dto.setIsAuthor(user.getIsAuthor());
        dto.setIsAdmin(user.getIsAdmin());
        dto.setLevel(user.getLevel());
        dto.setExp(user.getExp());
        dto.setReadTime(user.getReadTime());
        dto.setReadBookNum(user.getReadBookNum());
        dto.setCreateTime(user.getCreateTime());
        dto.setUpdateTime(user.getUpdateTime());
        dto.setLastActive(user.getLastActive());
        dto.setStatus(UserStatus.fromCode(user.getStatus()));
        return dto;
    }
}
