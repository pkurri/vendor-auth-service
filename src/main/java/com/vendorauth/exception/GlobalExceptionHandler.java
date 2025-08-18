package com.vendorauth.exception;

import com.vendorauth.dto.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status).body(buildErrorResponse(ex.getMessage(), status, request));
    }

    @ExceptionHandler({ExpiredJwtException.class, MalformedJwtException.class, SignatureException.class, UnsupportedJwtException.class})
    public ResponseEntity<ErrorResponse> handleTokenException(Exception ex, WebRequest request) {
        HttpStatus status;
        String message;
        if (ex instanceof ExpiredJwtException) {
            status = HttpStatus.UNAUTHORIZED;
            message = ex.getMessage() != null ? ex.getMessage() : "Token expired";
        } else if (ex instanceof MalformedJwtException) {
            status = HttpStatus.BAD_REQUEST;
            message = ex.getMessage() != null ? ex.getMessage() : "Invalid token";
        } else if (ex instanceof SignatureException) {
            status = HttpStatus.BAD_REQUEST;
            message = ex.getMessage() != null ? ex.getMessage() : "Invalid signature";
        } else {
            status = HttpStatus.BAD_REQUEST;
            message = ex.getMessage() != null ? ex.getMessage() : "Invalid token";
        }
        return ResponseEntity.status(status).body(buildErrorResponse(message, status, request));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
        String message = ex.getMessage() != null ? ex.getMessage() : "Method not supported";
        return ResponseEntity.status(status).body(buildErrorResponse(message, status, request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "An unexpected error occurred";
        return ResponseEntity.status(status).body(buildErrorResponse(message, status, request));
    }

    private ErrorResponse buildErrorResponse(String message, HttpStatus status, WebRequest request) {
        String path = request != null ? request.getDescription(false) : "";
        return ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
    }
}
