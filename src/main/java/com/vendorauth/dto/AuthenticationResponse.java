package com.vendorauth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response object for authentication attempts.
 * Provides detailed information about the authentication result.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationResponse {
    
    /**
     * Whether the authentication was successful
     */
    private boolean success;
    
    /**
     * Human-readable message describing the result
     */
    private String message;
    
    /**
     * Error code for failed authentications
     */
    private String errorCode;
    
    /**
     * Vendor ID that was authenticated against
     */
    private String vendorId;
    
    /**
     * Timestamp of the authentication attempt
     */
    private LocalDateTime timestamp;
    
    /**
     * Access token returned by the vendor (if applicable)
     */
    private String accessToken;
    
    /**
     * Refresh token returned by the vendor (if applicable)
     */
    private String refreshToken;
    
    /**
     * Token expiration time (if applicable)
     */
    private LocalDateTime tokenExpiry;
    
    /**
     * Additional response data from the vendor
     */
    private Map<String, Object> additionalData;
    
    /**
     * Creates a successful authentication response
     */
    public static AuthenticationResponse success(String vendorId, String message) {
        return AuthenticationResponse.builder()
                .success(true)
                .vendorId(vendorId)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Creates a failed authentication response
     */
    public static AuthenticationResponse failure(String vendorId, String message, String errorCode) {
        return AuthenticationResponse.builder()
                .success(false)
                .vendorId(vendorId)
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
