package com.vendorauth.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.vendorauth.dto.AuthenticationRequest;
import com.vendorauth.dto.AuthenticationResponse;
import com.vendorauth.entity.VendorConfig;
import com.vendorauth.exception.AuthenticationException;
import com.vendorauth.service.VendorAuthenticator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 implementation of VendorAuthenticator.
 * Handles OAuth2 authentication flows including client credentials and password grants.
 */
@Component
@Slf4j
public class OAuth2Authenticator implements VendorAuthenticator {
    
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public AuthenticationResponse authenticate(VendorConfig config, AuthenticationRequest request) {
        validateConfig(config);
        JsonNode authDetails = config.getAuthDetailsJson();
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            String grantType = authDetails.path("grantType").asText("client_credentials");
            body.add("grant_type", grantType);
            
            // Configure request based on grant type
            configureOAuth2Request(authDetails, request, body, grantType);
            
            // Execute token request
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
            var response = restTemplate.postForEntity(
                authDetails.path("tokenUrl").asText(), 
                entity, 
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return buildSuccessResponse(response.getBody());
            }
            
            throw new AuthenticationException("OAuth2 authentication failed: " + response.getStatusCode(), 
                "OAUTH2_AUTH_FAILED");
            
        } catch (Exception e) {
            log.error("OAuth2 authentication error", e);
            throw new AuthenticationException("OAuth2 authentication failed: " + e.getMessage(), 
                "OAUTH2_AUTH_ERROR", e);
        }
    }

    private void configureOAuth2Request(JsonNode authDetails, AuthenticationRequest request, 
                                      MultiValueMap<String, String> body, String grantType) {
        switch (grantType) {
            case "client_credentials":
                body.add("client_id", authDetails.path("clientId").asText());
                body.add("client_secret", authDetails.path("clientSecret").asText());
                if (authDetails.has("scope")) {
                    body.add("scope", authDetails.path("scope").asText());
                }
                break;
                
            case "password":
                body.add("username", request.getUsername());
                body.add("password", request.getPassword());
                body.add("client_id", authDetails.path("clientId").asText());
                if (authDetails.has("clientSecret")) {
                    body.add("client_secret", authDetails.path("clientSecret").asText());
                }
                break;
                
            default:
                throw new AuthenticationException("Unsupported grant type: " + grantType, 
                    "OAUTH2_UNSUPPORTED_GRANT_TYPE");
        }
    }
    
    private AuthenticationResponse buildSuccessResponse(Map<String, Object> responseBody) {
        String accessToken = firstNonNullString(responseBody, "access_token", "accessToken", "token");
        String refreshToken = firstNonNullString(responseBody, "refresh_token", "refreshToken");
        LocalDateTime tokenExpiry = extractExpiry(responseBody);

        return AuthenticationResponse.builder()
            .success(true)
            .message("Authentication successful")
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenExpiry(tokenExpiry)
            .additionalData(responseBody)
            .build();
    }

    private String firstNonNullString(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v != null) {
                String s = v.toString();
                if (!s.isEmpty()) return s;
            }
        }
        return null;
    }

    private LocalDateTime extractExpiry(Map<String, Object> map) {
        // Common OAuth2 fields: expires_in (seconds), or absolute expiry times
        Object expiresIn = map.get("expires_in");
        if (expiresIn != null) {
            try {
                long seconds = Long.parseLong(expiresIn.toString());
                return LocalDateTime.now().plusSeconds(seconds);
            } catch (NumberFormatException ignored) { }
        }
        // Fallback: no expiry available
        return null;
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
            throw new AuthenticationException("Invalid OAuth2 configuration", "OAUTH2_INVALID_CONFIG");
        }
        
        JsonNode authDetails = config.getAuthDetailsJson();
        if (!authDetails.has("tokenUrl")) {
            throw new AuthenticationException("Missing 'tokenUrl' in OAuth2 config", "OAUTH2_MISSING_TOKEN_URL");
        }
        
        String grantType = authDetails.path("grantType").asText("client_credentials");
        if ("client_credentials".equals(grantType) && 
            (!authDetails.has("clientId") || !authDetails.has("clientSecret"))) {
            throw new AuthenticationException(
                "client_credentials flow requires 'clientId' and 'clientSecret'", 
                "OAUTH2_MISSING_CREDENTIALS");
        }
    }

    @Override
    public String getRequiredConfigurationDescription() {
        return """
            OAuth2 Configuration (JSON format):
            {
              "tokenUrl": "https://auth.example.com/oauth/token",
              "grantType": "client_credentials|password|authorization_code",
              "clientId": "your-client-id",
              "clientSecret": "your-client-secret",
              "scope": "optional space-separated scopes"
            }
            """;
    }
}
