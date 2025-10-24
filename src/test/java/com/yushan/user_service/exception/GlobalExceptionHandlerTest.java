package com.yushan.user_service.exception;

import com.yushan.user_service.dto.ApiResponse;
import com.yushan.user_service.enums.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleAuthorizationDeniedException() {
        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleAuthorizationDeniedException(webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), response.getBody().getCode());
        assertEquals("Access denied", response.getBody().getMessage());
    }

    @Test
    void testHandleResourceNotFoundException() {
        // Given
        String errorMessage = "Resource not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(errorMessage);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleResourceNotFoundException(exception, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.NOT_FOUND.getCode(), response.getBody().getCode());
        assertEquals(errorMessage, response.getBody().getMessage());
    }

    @Test
    void testHandleValidationException() {
        // Given
        String errorMessage = "Validation failed";
        ValidationException exception = new ValidationException(errorMessage);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleValidationException(exception, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.BAD_REQUEST.getCode(), response.getBody().getCode());
        assertEquals(errorMessage, response.getBody().getMessage());
    }

    @Test
    void testHandleUnauthorizedException() {
        // Given
        String errorMessage = "User is not authorized";
        UnauthorizedException exception = new UnauthorizedException(errorMessage);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleUnauthorizedException(exception, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.UNAUTHORIZED.getCode(), response.getBody().getCode());
        assertEquals(errorMessage, response.getBody().getMessage());
    }

    @Test
    void testHandleForbiddenException() {
        // Given
        String errorMessage = "Access to this resource is forbidden";
        ForbiddenException exception = new ForbiddenException(errorMessage);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleForbiddenException(exception, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.FORBIDDEN.getCode(), response.getBody().getCode());
        assertEquals(errorMessage, response.getBody().getMessage());
    }

    @Test
    void testHandleMethodArgumentNotValidException() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("object", "field1", "Field 1 is required");
        FieldError fieldError2 = new FieldError("object", "field2", "Field 2 is invalid");
        List<FieldError> fieldErrors = Arrays.asList(fieldError1, fieldError2);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleValidationException(exception, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.BAD_REQUEST.getCode(), response.getBody().getCode());
        assertEquals("Field 1 is required; Field 2 is invalid", response.getBody().getMessage());
    }

    @Test
    void testHandleMethodArgumentNotValidExceptionWithSingleError() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "Field is required");
        List<FieldError> fieldErrors = Arrays.asList(fieldError);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleValidationException(exception, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.BAD_REQUEST.getCode(), response.getBody().getCode());
        assertEquals("Field is required", response.getBody().getMessage());
    }

    @Test
    void testHandleBindException() {
        // Given
        BindException exception = mock(BindException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("object", "field1", "Field 1 is required");
        FieldError fieldError2 = new FieldError("object", "field2", "Field 2 is invalid");
        List<FieldError> fieldErrors = Arrays.asList(fieldError1, fieldError2);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleBindException(exception, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.BAD_REQUEST.getCode(), response.getBody().getCode());
        assertEquals("Field 1 is required; Field 2 is invalid", response.getBody().getMessage());
    }

    @Test
    void testHandleBindExceptionWithSingleError() {
        // Given
        BindException exception = mock(BindException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "Field is required");
        List<FieldError> fieldErrors = Arrays.asList(fieldError);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleBindException(exception, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.BAD_REQUEST.getCode(), response.getBody().getCode());
        assertEquals("Field is required", response.getBody().getMessage());
    }

    @Test
    void testHandleException() {
        // Given
        String errorMessage = "Something went wrong";
        Exception exception = new Exception(errorMessage);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleException(exception, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), response.getBody().getCode());
        assertEquals("System error: " + errorMessage, response.getBody().getMessage());
    }

    @Test
    void testHandleExceptionWithNullMessage() {
        // Given
        Exception exception = new Exception();

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleException(exception, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), response.getBody().getCode());
        assertEquals("System error: null", response.getBody().getMessage());
    }

    @Test
    void testHandleMethodArgumentNotValidExceptionWithEmptyErrors() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        List<FieldError> fieldErrors = Arrays.asList();

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleValidationException(exception, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.BAD_REQUEST.getCode(), response.getBody().getCode());
        assertEquals("", response.getBody().getMessage());
    }

    @Test
    void testHandleBindExceptionWithEmptyErrors() {
        // Given
        BindException exception = mock(BindException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        List<FieldError> fieldErrors = Arrays.asList();

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleBindException(exception, webRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.BAD_REQUEST.getCode(), response.getBody().getCode());
        assertEquals("", response.getBody().getMessage());
    }
}
