package com.yushan.user_service.interceptor;

import com.yushan.user_service.event.UserActivityEventProducer;
import com.yushan.user_service.event.dto.UserActivityEvent;
import com.yushan.user_service.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserActivityInterceptorTest {

    @Mock
    private UserActivityEventProducer userActivityEventProducer;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private CustomUserDetailsService.CustomUserDetails userDetails;

    @InjectMocks
    private UserActivityInterceptor userActivityInterceptor;

    private MockHttpServletRequest mockRequest;
    private MockHttpServletResponse mockResponse;
    private final UUID testUserId = UUID.randomUUID();
    private final String requestUri = "/api/v1/users/profile";
    private final String requestMethod = "GET";

    @BeforeEach
    void setUp() {
        mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI(requestUri);
        mockRequest.setMethod(requestMethod);
        mockResponse = new MockHttpServletResponse();

        // Mock the SecurityContextHolder to avoid static method issues
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void preHandle_whenUserIsAuthenticated_shouldSendActivityEventAndReturnTrue() throws Exception {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUserId()).thenReturn(testUserId.toString());

        // When
        boolean result = userActivityInterceptor.preHandle(mockRequest, mockResponse, new Object());

        // Then
        assertTrue(result, "Interceptor should always return true to not block the request chain.");

        ArgumentCaptor<UserActivityEvent> eventCaptor = ArgumentCaptor.forClass(UserActivityEvent.class);
        verify(userActivityEventProducer, times(1)).sendUserActivityEvent(eventCaptor.capture());

        UserActivityEvent capturedEvent = eventCaptor.getValue();
        assertEquals(testUserId, capturedEvent.userId());
        assertEquals("user-service", capturedEvent.serviceName());
        assertEquals(requestUri, capturedEvent.endpoint());
        assertEquals(requestMethod, capturedEvent.method());
        assertNotNull(capturedEvent.timestamp());
    }

    @Test
    void preHandle_whenUserIsNotAuthenticated_shouldNotSendEventAndReturnTrue() throws Exception {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);

        // When
        boolean result = userActivityInterceptor.preHandle(mockRequest, mockResponse, new Object());

        // Then
        assertTrue(result);
        verifyNoInteractions(userActivityEventProducer);
    }

    @Test
    void preHandle_whenPrincipalIsAnonymousString_shouldNotSendEventAndReturnTrue() throws Exception {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        // When
        boolean result = userActivityInterceptor.preHandle(mockRequest, mockResponse, new Object());

        // Then
        assertTrue(result);
        verifyNoInteractions(userActivityEventProducer);
    }

    @Test
    void preHandle_whenUserIdIsInvalidFormat_shouldNotSendEventAndReturnTrue() throws Exception {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUserId()).thenReturn("invalid-uuid-format");

        // When
        boolean result = userActivityInterceptor.preHandle(mockRequest, mockResponse, new Object());

        // Then
        assertTrue(result);
        verifyNoInteractions(userActivityEventProducer);
    }

    @Test
    void preHandle_whenEventProducerThrowsException_shouldCatchExceptionAndReturnTrue() throws Exception {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUserId()).thenReturn(testUserId.toString());
        doThrow(new RuntimeException("Kafka is down")).when(userActivityEventProducer).sendUserActivityEvent(any(UserActivityEvent.class));

        // When
        boolean result = userActivityInterceptor.preHandle(mockRequest, mockResponse, new Object());

        // Then
        assertTrue(result, "Interceptor must return true even if event sending fails.");
        verify(userActivityEventProducer, times(1)).sendUserActivityEvent(any(UserActivityEvent.class));
    }

    @Test
    void preHandle_whenAuthenticationIsNull_shouldNotSendEventAndReturnTrue() throws Exception {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        boolean result = userActivityInterceptor.preHandle(mockRequest, mockResponse, new Object());

        // Then
        assertTrue(result);
        verifyNoInteractions(userActivityEventProducer);
    }
}
