package com.vendorauth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;

/**
 * DTO for JWT authentication response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtAuthenticationResponse {
    private String token;
    private String refreshToken;
    @Default
    private String tokenType = "Bearer";
    private long expiresIn;
    
    public JwtAuthenticationResponse(String token, String refreshToken, long expiresIn) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }
}
