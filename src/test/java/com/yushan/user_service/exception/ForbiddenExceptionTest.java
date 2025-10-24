package com.yushan.user_service.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ForbiddenException.
 */
class ForbiddenExceptionTest {

    @Test
    void testForbiddenExceptionWithMessage() {
        // Given
        String message = "Access forbidden";

        // When
        ForbiddenException exception = new ForbiddenException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testForbiddenExceptionWithMessageAndCause() {
        // Given
        String message = "Access forbidden";
        Throwable cause = new RuntimeException("Root cause");

        // When
        ForbiddenException exception = new ForbiddenException(message, cause);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testForbiddenExceptionInheritance() {
        // Given
        String message = "Access forbidden";

        // When
        ForbiddenException exception = new ForbiddenException(message);

        // Then
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testResponseStatusAnnotation() {
        // Given
        ResponseStatus responseStatus = ForbiddenException.class.getAnnotation(ResponseStatus.class);

        // Then
        assertNotNull(responseStatus);
        assertEquals(HttpStatus.FORBIDDEN, responseStatus.value());
    }

    @Test
    void testForbiddenExceptionWithNullMessage() {
        // When
        ForbiddenException exception = new ForbiddenException(null);

        // Then
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void testForbiddenExceptionWithEmptyMessage() {
        // Given
        String message = "";

        // When
        ForbiddenException exception = new ForbiddenException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }
}
