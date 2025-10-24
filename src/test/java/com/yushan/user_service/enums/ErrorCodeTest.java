package com.yushan.user_service.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ErrorCode enum.
 */
class ErrorCodeTest {

    @Test
    void testSuccessErrorCode() {
        // Given & When
        ErrorCode errorCode = ErrorCode.SUCCESS;

        // Then
        assertEquals(200, errorCode.getCode());
        assertEquals("Success", errorCode.getMessage());
    }

    @Test
    void testBadRequestErrorCode() {
        // Given & When
        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        // Then
        assertEquals(400, errorCode.getCode());
        assertEquals("Bad Request", errorCode.getMessage());
    }

    @Test
    void testUnauthorizedErrorCode() {
        // Given & When
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        // Then
        assertEquals(401, errorCode.getCode());
        assertEquals("Unauthorized", errorCode.getMessage());
    }

    @Test
    void testForbiddenErrorCode() {
        // Given & When
        ErrorCode errorCode = ErrorCode.FORBIDDEN;

        // Then
        assertEquals(403, errorCode.getCode());
        assertEquals("Forbidden", errorCode.getMessage());
    }

    @Test
    void testNotFoundErrorCode() {
        // Given & When
        ErrorCode errorCode = ErrorCode.NOT_FOUND;

        // Then
        assertEquals(404, errorCode.getCode());
        assertEquals("Not Found", errorCode.getMessage());
    }

    @Test
    void testInternalServerErrorCode() {
        // Given & When
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        // Then
        assertEquals(500, errorCode.getCode());
        assertEquals("Internal Server Error", errorCode.getMessage());
    }

    @Test
    void testErrorCodeValues() {
        // Given & When
        ErrorCode[] values = ErrorCode.values();

        // Then
        assertEquals(7, values.length);
        assertArrayEquals(new ErrorCode[]{
            ErrorCode.SUCCESS,
            ErrorCode.BAD_REQUEST,
            ErrorCode.UNAUTHORIZED,
            ErrorCode.FORBIDDEN,
            ErrorCode.NOT_FOUND,
            ErrorCode.VALIDATION_ERROR,
            ErrorCode.INTERNAL_SERVER_ERROR,
        }, values);
    }

    @Test
    void testErrorCodeValueOf() {
        // Given & When
        ErrorCode success = ErrorCode.valueOf("SUCCESS");
        ErrorCode badRequest = ErrorCode.valueOf("BAD_REQUEST");
        ErrorCode internalServerError = ErrorCode.valueOf("INTERNAL_SERVER_ERROR");

        // Then
        assertEquals(ErrorCode.SUCCESS, success);
        assertEquals(ErrorCode.BAD_REQUEST, badRequest);
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, internalServerError);
    }

    @Test
    void testErrorCodeValueOfInvalid() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            ErrorCode.valueOf("INVALID_ERROR_CODE");
        });
    }
}
