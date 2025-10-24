package com.yushan.user_service.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationException.
 */
class ValidationExceptionTest {

    @Test
    void testValidationExceptionWithMessage() {
        // Given
        String message = "Validation failed";

        // When
        ValidationException exception = new ValidationException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testValidationExceptionWithMessageAndCause() {
        // Given
        String message = "Validation failed";
        Throwable cause = new RuntimeException("Root cause");

        // When
        ValidationException exception = new ValidationException(message, cause);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testValidationExceptionInheritance() {
        // Given
        String message = "Validation failed";

        // When
        ValidationException exception = new ValidationException(message);

        // Then
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testResponseStatusAnnotation() {
        // Given
        ResponseStatus responseStatus = ValidationException.class.getAnnotation(ResponseStatus.class);

        // Then
        assertNotNull(responseStatus);
        assertEquals(HttpStatus.BAD_REQUEST, responseStatus.value());
    }

    @Test
    void testValidationExceptionWithNullMessage() {
        // When
        ValidationException exception = new ValidationException(null);

        // Then
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void testValidationExceptionWithEmptyMessage() {
        // Given
        String message = "";

        // When
        ValidationException exception = new ValidationException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }
}
