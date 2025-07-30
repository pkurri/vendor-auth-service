package com.vendorauth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Generic authentication request that can accommodate various authentication mechanisms.
 * This flexible structure allows different vendors to send different types of credentials.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationRequest {
    
    /**
     * Username for basic authentication scenarios
     */
    private String username;
    
    /**
     * Password for basic authentication scenarios
     */
    private String password;
    
    /**
     * Token for token-based authentication (API key, JWT, etc.)
     */
    private String token;
    
    /**
     * Additional authentication parameters specific to the vendor.
     * This allows for maximum flexibility in handling unknown authentication details.
     */
    private Map<String, Object> additionalParams;
    
    /**
     * Client identifier for OAuth2 flows
     */
    private String clientId;
    
    /**
     * Client secret for OAuth2 flows
     */
    private String clientSecret;
    
    /**
     * Authorization code for OAuth2 authorization code flow
     */
    private String authorizationCode;
    
    /**
     * Redirect URI for OAuth2 flows
     */
    private String redirectUri;
}
