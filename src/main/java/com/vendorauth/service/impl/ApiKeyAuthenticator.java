package com.vendorauth.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vendorauth.dto.AuthenticationRequest;
import com.vendorauth.dto.AuthenticationResponse;
import com.vendorauth.entity.VendorConfig;
import com.vendorauth.enums.AuthType;
import com.vendorauth.exception.AuthenticationException;
import com.vendorauth.service.VendorAuthenticator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * API Key-based authenticator implementation.
 * 
 * This authenticator handles vendors that use API key authentication.
 * The API key can be sent via header or query parameter.
 * 
 * Expected authDetailsJson format:
 * {
 *   "apiKeyHeader": "X-API-Key",           // Header name for API key
 *   "apiKeyQueryParam": "api_key",         // Query parameter name for API key
 *   "authMethod": "header"                 // "header" or "query"
 * }
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthenticator implements VendorAuthenticator {
    
    private final ObjectMapper objectMapper;
    
    @Override
    public AuthenticationResponse authenticate(VendorConfig config, AuthenticationRequest request) {
        log.info("ApiKeyAuthenticator: Authenticating for vendor: {}", config.getVendorId());
        
        try {
            // Parse vendor configuration
            JsonNode authDetails = objectMapper.readTree(config.getAuthDetailsJson());
            // Note: authMethod could be used for different API key transmission methods
            String authMethod = authDetails.path("authMethod").asText("header");
            
            // Validate that API key is provided in the request
            if (request.getToken() == null || request.getToken().trim().isEmpty()) {
                return AuthenticationResponse.failure(
                    config.getVendorId(),
                    "API key is required but not provided",
                    "MISSING_API_KEY"
                );
            }
            
            // TODO: In a real implementation, you would:
            // 1. Make HTTP request to vendor's API with the API key
            // 2. Validate the response
            // 3. Return appropriate success/failure response
            
            // For now, simulate API key validation
            if (isValidApiKey(request.getToken(), config)) {
                return AuthenticationResponse.builder()
                        .success(true)
                        .vendorId(config.getVendorId())
                        .message("API key authentication successful")
                        .accessToken(request.getToken()) // Echo back the API key
                        .timestamp(LocalDateTime.now())
                        .build();
            } else {
                return AuthenticationResponse.failure(
                    config.getVendorId(),
                    "Invalid API key",
                    "INVALID_API_KEY"
                );
            }
            
        } catch (Exception e) {
            log.error("Error during API key authentication for vendor: {}", config.getVendorId(), e);
            throw new AuthenticationException(
                config.getVendorId(),
                "API_KEY_AUTH_ERROR",
                "Failed to authenticate with API key: " + e.getMessage(),
                e
            );
        }
    }
    
    @Override
    public boolean isConfigurationValid(VendorConfig config) {
        if (config == null || config.getAuthType() != AuthType.API_KEY) {
            return false;
        }
        
        try {
            JsonNode authDetails = objectMapper.readTree(config.getAuthDetailsJson());
            
            // Check that either header or query param is specified
            boolean hasHeader = authDetails.has("apiKeyHeader") && 
                               !authDetails.path("apiKeyHeader").asText().isEmpty();
            boolean hasQueryParam = authDetails.has("apiKeyQueryParam") && 
                                   !authDetails.path("apiKeyQueryParam").asText().isEmpty();
            
            return hasHeader || hasQueryParam;
            
        } catch (Exception e) {
            log.error("Invalid configuration for API key authenticator: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getRequiredConfigurationDescription() {
        return "API Key authenticator requires JSON configuration with:\n" +
               "{\n" +
               "  \"apiKeyHeader\": \"X-API-Key\",           // Header name for API key\n" +
               "  \"apiKeyQueryParam\": \"api_key\",         // Query parameter name for API key\n" +
               "  \"authMethod\": \"header\"                 // \"header\" or \"query\"\n" +
               "}\n" +
               "At least one of apiKeyHeader or apiKeyQueryParam must be specified.";
    }
    
    /**
     * Simulates API key validation.
     * In a real implementation, this would make an HTTP request to the vendor's API.
     */
    private boolean isValidApiKey(String apiKey, VendorConfig config) {
        // Simple validation for demonstration
        // In reality, you would call the vendor's API to validate the key
        return apiKey.length() >= 10 && !apiKey.equals("invalid_key");
    }
}
