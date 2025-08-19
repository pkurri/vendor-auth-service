package com.vendorauth.exception;

import com.vendorauth.dto.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.request.WebRequest;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("test-uri");
    }

    @Test
    void handleBadCredentialsException() {
        BadCredentialsException ex = new BadCredentialsException("Invalid credentials");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadCredentialsException(ex, webRequest);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().getStatus());
    }

    @Test
    void handleTokenException_ExpiredJwt() {
        ExpiredJwtException ex = new ExpiredJwtException(null, null, "Token expired");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleTokenException(ex, webRequest);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).getMessage().contains("expired"));
    }

    @Test
    void handleTokenException_MalformedJwt() {
        MalformedJwtException ex = new MalformedJwtException("Invalid token");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleTokenException(ex, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).getMessage().contains("invalid"));
    }

    @Test
    void handleTokenException_SignatureException() {
        SignatureException ex = new SignatureException("Invalid signature");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleTokenException(ex, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).getMessage().contains("signature"));
    }

    @Test
    void handleMethodNotSupported() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException(HttpMethod.POST, java.util.List.of());
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodNotSupported(ex, webRequest);
        
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).getMessage().contains("not supported"));
    }

    @Test
    void handleAllExceptions() {
        Exception ex = new Exception("Test exception");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAllExceptions(ex, webRequest);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", Objects.requireNonNull(response.getBody()).getMessage());
    }
}
