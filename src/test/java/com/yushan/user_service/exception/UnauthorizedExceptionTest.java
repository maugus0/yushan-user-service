package com.yushan.user_service.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UnauthorizedException.
 */
class UnauthorizedExceptionTest {

    @Test
    void testUnauthorizedExceptionWithMessage() {
        // Given
        String message = "User not authorized";

        // When
        UnauthorizedException exception = new UnauthorizedException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testUnauthorizedExceptionWithMessageAndCause() {
        // Given
        String message = "User not authorized";
        Throwable cause = new RuntimeException("Root cause");

        // When
        UnauthorizedException exception = new UnauthorizedException(message, cause);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testUnauthorizedExceptionInheritance() {
        // Given
        String message = "User not authorized";

        // When
        UnauthorizedException exception = new UnauthorizedException(message);

        // Then
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testResponseStatusAnnotation() {
        // Given
        ResponseStatus responseStatus = UnauthorizedException.class.getAnnotation(ResponseStatus.class);

        // Then
        assertNotNull(responseStatus);
        assertEquals(HttpStatus.UNAUTHORIZED, responseStatus.value());
    }

    @Test
    void testUnauthorizedExceptionWithNullMessage() {
        // When
        UnauthorizedException exception = new UnauthorizedException(null);

        // Then
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void testUnauthorizedExceptionWithEmptyMessage() {
        // Given
        String message = "";

        // When
        UnauthorizedException exception = new UnauthorizedException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }
}
