package com.yushan.user_service.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ResourceNotFoundException.
 */
class ResourceNotFoundExceptionTest {

    @Test
    void testResourceNotFoundExceptionWithMessage() {
        // Given
        String message = "Resource not found";

        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testResourceNotFoundExceptionWithMessageAndCause() {
        // Given
        String message = "Resource not found";
        Throwable cause = new RuntimeException("Root cause");

        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(message, cause);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testResourceNotFoundExceptionInheritance() {
        // Given
        String message = "Resource not found";

        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        // Then
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testResponseStatusAnnotation() {
        // Given
        ResponseStatus responseStatus = ResourceNotFoundException.class.getAnnotation(ResponseStatus.class);

        // Then
        assertNotNull(responseStatus);
        assertEquals(HttpStatus.NOT_FOUND, responseStatus.value());
    }

    @Test
    void testResourceNotFoundExceptionWithNullMessage() {
        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(null);

        // Then
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void testResourceNotFoundExceptionWithEmptyMessage() {
        // Given
        String message = "";

        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }
}
