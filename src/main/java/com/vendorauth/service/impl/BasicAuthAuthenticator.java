package com.vendorauth.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.vendorauth.dto.AuthenticationRequest;
import com.vendorauth.dto.AuthenticationResponse;
import com.vendorauth.entity.VendorConfig;
import com.vendorauth.exception.AuthenticationException;
import com.vendorauth.service.VendorAuthenticator;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic Authentication implementation of VendorAuthenticator.
 * Handles HTTP Basic Authentication with external services.
 */
@Component
@Slf4j
public class BasicAuthAuthenticator implements VendorAuthenticator {
    
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public AuthenticationResponse authenticate(VendorConfig config, AuthenticationRequest request) {
        validateConfig(config);
        JsonNode authDetails = config.getAuthDetailsJson();
        
        try {
            String username = request.getUsername() != null ? 
                request.getUsername() : authDetails.path("username").asText();
                
            String password = request.getPassword() != null ? 
                request.getPassword() : authDetails.path("password").asText();
            
            String authUrl = authDetails.path("authUrl").asText();
            
            // Create headers with Basic Auth
            HttpHeaders headers = createBasicAuthHeaders(username, password);
            
            // Make the request
            HttpEntity<String> entity = new HttpEntity<>(headers);
            var response = restTemplate.exchange(
                authUrl,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return AuthenticationResponse.builder()
                    .success(true)
                    .message("Basic authentication successful")
                    .build();
            }
            
            throw new AuthenticationException("Basic authentication failed: " + response.getStatusCode(), 
                "BASIC_AUTH_FAILED");
            
        } catch (Exception e) {
            log.error("Basic authentication error", e);
            throw new AuthenticationException("Basic authentication failed: " + e.getMessage(), 
                "BASIC_AUTH_ERROR", e);
        }
    }
    
    private HttpHeaders createBasicAuthHeaders(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Override
    public boolean isConfigurationValid(VendorConfig config) {
        try {
            validateConfig(config);
            return true;
        } catch (AuthenticationException e) {
            return false;
        }
    }

    private void validateConfig(VendorConfig config) {
        if (config == null || config.getAuthDetailsJson() == null) {
            throw new AuthenticationException("Invalid Basic Auth configuration", "BASIC_AUTH_INVALID_CONFIG");
        }
        
        JsonNode authDetails = config.getAuthDetailsJson();
        
        // Either credentials must be in config, or they must be provided in the request
        boolean hasConfigCredentials = authDetails.has("username") && authDetails.has("password");
        boolean requiresRequestCredentials = authDetails.has("authUrl") && 
                                           !authDetails.path("requireConfigCredentials").asBoolean(true);
        
        if (!hasConfigCredentials && !requiresRequestCredentials) {
            throw new AuthenticationException(
                "Basic Auth requires either username/password in config or in request", 
                "BASIC_AUTH_MISSING_CREDENTIALS");
        }
        
        if (!authDetails.has("authUrl")) {
            throw new AuthenticationException(
                "Missing 'authUrl' in Basic Auth configuration", 
                "BASIC_AUTH_MISSING_AUTH_URL");
        }
    }

    @Override
    public String getRequiredConfigurationDescription() {
        return """
            Basic Authentication Configuration (JSON format):
            {
              "authUrl": "https://api.example.com/auth/validate",
              "username": "optional-username",  // Can be provided in request instead
              "password": "optional-password",  // Can be provided in request instead
              "requireConfigCredentials": true  // Whether username/password must be in config
            }
            
            Note: Either provide username/password in the config OR set requireConfigCredentials 
            to false and provide them in each authentication request.
            """;
    }
}
