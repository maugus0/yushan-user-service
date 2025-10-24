package com.yushan.user_service.listener;
import com.yushan.user_service.event.dto.UserActivityEvent;
import com.yushan.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserActivityListenerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserActivityListener userActivityListener;

    private UserActivityEvent userActivityEvent;

    @Test
    void handleUserActivity_shouldUpdateLastActiveTime_whenUserIdIsNotNull() {
        // Given
        UUID userId = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();
        userActivityEvent = new UserActivityEvent(userId, "", "", "", timestamp);

        // When
        userActivityListener.handleUserActivity(userActivityEvent);

        // Then
        verify(userService, times(1)).updateLastActiveTime(userId, timestamp);
    }

    @Test
    void handleUserActivity_shouldNotUpdateLastActiveTime_whenUserIdIsNull() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        userActivityEvent = new UserActivityEvent(null, "", "", "", timestamp);

        // When
        userActivityListener.handleUserActivity(userActivityEvent);

        // Then
        verify(userService, never()).updateLastActiveTime(any(), any(LocalDateTime.class));
    }
}
