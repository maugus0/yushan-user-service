package com.yushan.user_service.service;

import com.yushan.user_service.dao.LibraryMapper;
import com.yushan.user_service.dao.UserMapper;
import com.yushan.user_service.dto.UserRegistrationRequestDTO;
import com.yushan.user_service.dto.UserAuthResponseDTO;
import com.yushan.user_service.entity.Library;
import com.yushan.user_service.entity.User;
import com.yushan.user_service.enums.Gender;
import com.yushan.user_service.enums.UserStatus;
import com.yushan.user_service.event.UserEventProducer;
import com.yushan.user_service.event.dto.UserLoggedInEvent;
import com.yushan.user_service.event.dto.UserRegisteredEvent;
import com.yushan.user_service.exception.ValidationException;
import com.yushan.user_service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LibraryMapper libraryMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserEventProducer userEventProducer;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    /**
     * register a new user
     * @param registrationDTO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public User register(UserRegistrationRequestDTO registrationDTO) {
        // check if email existed
        if (userMapper.selectByEmail(registrationDTO.getEmail()) != null) {
            throw new RuntimeException("email was registered");
        }

        User user = new User();
        user.setUuid(UUID.randomUUID());
        user.setEmail(registrationDTO.getEmail());
        user.setUsername(registrationDTO.getUsername());
        user.setHashPassword(hashPassword(registrationDTO.getPassword()));

        Gender gender = registrationDTO.getGender();
        user.setGender(gender.getCode());
        user.setAvatarUrl(gender.getAvatarUrl());

        if (registrationDTO.getBirthday() != null) {
            user.setBirthday(registrationDTO.getBirthday());
        } else {
            user.setBirthday(null);
        }

        Date date = new Date();
        user.setCreateTime(date);
        user.setUpdateTime(date);
        user.setLastLogin(date);
        user.setLastActive(date);

        // set default user profile
        user.setStatus(UserStatus.NORMAL.getCode());
        user.setIsAuthor(false);
        user.setIsAdmin(false);

        userMapper.insert(user);

        // create user library
        Library library = new Library();
        library.setUuid(UUID.randomUUID());
        library.setUserId(user.getUuid());

        libraryMapper.insertSelective(library);
        return user;
    }

    /**
     * register a new user and create response
     * @param registrationDTO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public UserAuthResponseDTO registerAndCreateResponse(UserRegistrationRequestDTO registrationDTO) {

        User user = register(registrationDTO);

        UserRegisteredEvent event = new UserRegisteredEvent(
                user.getUuid(),
                user.getUsername(),
                user.getEmail(),
                user.getCreateTime(),
                user.getUpdateTime(),
                user.getLastLogin(),
                user.getLastActive()
        );
        userEventProducer.sendUserRegisteredEvent(event);

        // Generate JWT tokens for auto-login after registration
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        UserAuthResponseDTO responseDTO = createUserResponse(user);
        responseDTO.setAccessToken(accessToken);
        responseDTO.setRefreshToken(refreshToken);
        responseDTO.setTokenType("Bearer");
        responseDTO.setExpiresIn(accessTokenExpiration);
        return responseDTO;
    }

    /**
     * login a user
     * @param email
     * @param password
     * @return
     */
    public User login(String email, String password) {
        User user = userMapper.selectByEmail(email);
        if (user != null && BCrypt.checkpw(password, user.getHashPassword())) {
            // Check if user is suspended or banned
            UserStatus status = UserStatus.fromCode(user.getStatus());
            if (status == UserStatus.SUSPENDED) {
                throw new ValidationException("Account is suspended. Please contact support.");
            }
            if (status == UserStatus.BANNED) {
                throw new ValidationException("Account is banned. Please contact support.");
            }
            return user;
        }
        else {
            throw new ValidationException("Invalid email or password");
        }
    }

    /**
     * login a user and create response
     * @param email
     * @param password
     * @return
     */
    public UserAuthResponseDTO loginAndCreateResponse(String email, String password) {
        User user = login(email, password);

        // Generate JWT tokens
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        UserLoggedInEvent event = new UserLoggedInEvent(
                user.getUuid(),
                user.getUsername(),
                user.getEmail(),
                user.getCreateTime(),
                user.getUpdateTime(),
                user.getLastLogin(),
                user.getLastActive()
        );
        userEventProducer.sendUserLoggedInEvent(event);

        Date now = new Date();
        user.setLastLogin(now);
        user.setLastActive(now);

        // Prepare user info (without sensitive data)
        UserAuthResponseDTO responseDTO = createUserResponse(user);
        responseDTO.setAccessToken(accessToken);
        responseDTO.setRefreshToken(refreshToken);
        responseDTO.setTokenType("Bearer");
        responseDTO.setExpiresIn(accessTokenExpiration);

        userMapper.updateByPrimaryKeySelective(user);
        return responseDTO;
    }

    /**
     * refresh token and create response
     * @param refreshToken
     * @return
     */
    public UserAuthResponseDTO refreshToken(String refreshToken) {
        // Validate refresh token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new ValidationException("Invalid refresh token");
        }

        // Check if it's actually a refresh token
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new ValidationException("Token is not a refresh token");
        }

        // Extract user info from refresh token
        String email = jwtUtil.extractEmail(refreshToken);
        String userId = jwtUtil.extractUserId(refreshToken);

        // Load user from database
        User user = userMapper.selectByEmail(email);

        if (user == null || !user.getUuid().toString().equals(userId)) {
            throw new ValidationException("User not found or token mismatch");
        }

        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(user);

        // Optionally generate new refresh token (token rotation)
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        UserAuthResponseDTO responseDTO = createUserResponse(user);
        responseDTO.setAccessToken(newAccessToken);
        responseDTO.setRefreshToken(newRefreshToken);
        responseDTO.setTokenType("Bearer");
        responseDTO.setExpiresIn(accessTokenExpiration);
        return responseDTO;
    }

    /**
     * hash password
     * @param password
     * @return
     */
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * create user response (without sensitive data)
     * @param user
     * @return
     */
    private UserAuthResponseDTO createUserResponse(User user) {
        UserAuthResponseDTO dto = new UserAuthResponseDTO();
        dto.setUuid(user.getUuid().toString());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setProfileDetail(user.getProfileDetail());
        dto.setBirthday(user.getBirthday());
        dto.setGender(Gender.fromCode(user.getGender()));
        dto.setStatus(UserStatus.fromCode(user.getStatus()));
        dto.setIsAuthor(user.getIsAuthor());
        dto.setIsAdmin(user.getIsAdmin());
        dto.setCreateTime(user.getCreateTime());
        dto.setUpdateTime(user.getUpdateTime());
        dto.setLastActive(user.getLastActive());
        dto.setLastLogin(user.getLastLogin());
        return dto;
    }
}