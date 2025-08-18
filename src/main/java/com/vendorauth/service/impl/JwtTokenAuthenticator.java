package com.vendorauth.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vendorauth.dto.AuthenticationRequest;
import com.vendorauth.dto.AuthenticationResponse;
import com.vendorauth.entity.VendorConfig;
import com.vendorauth.enums.AuthType;
import com.vendorauth.exception.AuthenticationException;
import com.vendorauth.service.VendorAuthenticator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token-based authenticator implementation.
 * 
 * This authenticator handles vendors that use JWT tokens for authentication.
 * It can validate existing JWT tokens or generate new ones based on vendor configuration.
 * 
 * Expected authDetailsJson format:
 * {
 *   "secretKey": "your-secret-key-here",           // Secret key for JWT validation/signing
 *   "issuer": "vendor-name",                       // Expected issuer claim
 *   "audience": "api-service",                     // Expected audience claim
 *   "algorithm": "HS256",                          // JWT algorithm (HS256, HS512, etc.)
 *   "expirationMinutes": 60,                       // Token expiration in minutes
 *   "validateExpiration": true,                    // Whether to validate token expiration
 *   "validateIssuer": true,                        // Whether to validate issuer claim
 *   "validateAudience": true                       // Whether to validate audience claim
 * }
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenAuthenticator implements VendorAuthenticator {
    
    private final ObjectMapper objectMapper;
    
    @Override
    public AuthenticationResponse authenticate(VendorConfig config, AuthenticationRequest request) {
        log.info("JwtTokenAuthenticator: Authenticating for vendor: {}", config.getVendorId());
        
        try {
            // Parse vendor configuration
            JsonNode authDetails = objectMapper.readTree(config.getAuthDetailsJson());
            
            // Validate that JWT token is provided in the request
            if (request.getToken() == null || request.getToken().trim().isEmpty()) {
                return AuthenticationResponse.failure(
                    config.getVendorId(),
                    "JWT token is required but not provided",
                    "MISSING_JWT_TOKEN"
                );
            }
            
            // Validate JWT token
            Claims claims = validateJwtToken(request.getToken(), authDetails, config.getVendorId());
            
            if (claims != null) {
                // Token is valid, create success response
                return AuthenticationResponse.builder()
                        .success(true)
                        .vendorId(config.getVendorId())
                        .message("JWT token authentication successful")
                        .accessToken(request.getToken())
                        .tokenExpiry(getTokenExpiry(claims))
                        .build();
            } else {
                return AuthenticationResponse.failure(
                    config.getVendorId(),
                    "Invalid or expired JWT token",
                    "INVALID_JWT_TOKEN"
                );
            }
            
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token for vendor: {}", config.getVendorId());
            return AuthenticationResponse.failure(
                config.getVendorId(),
                "JWT token has expired",
                "EXPIRED_JWT_TOKEN"
            );
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token for vendor: {}", config.getVendorId());
            return AuthenticationResponse.failure(
                config.getVendorId(),
                "Malformed JWT token",
                "MALFORMED_JWT_TOKEN"
            );
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature for vendor: {}", config.getVendorId());
            return AuthenticationResponse.failure(
                config.getVendorId(),
                "Invalid JWT token signature",
                "INVALID_JWT_SIGNATURE"
            );
        } catch (Exception e) {
            log.error("Error during JWT authentication for vendor: {}", config.getVendorId(), e);
            throw new AuthenticationException(
                config.getVendorId(),
                "JWT_AUTH_ERROR",
                "Failed to authenticate with JWT token: " + e.getMessage(),
                e
            );
        }
    }
    
    @Override
    public boolean isConfigurationValid(VendorConfig config) {
        if (config == null || config.getAuthType() != AuthType.CUSTOM) {
            return false;
        }
        
        try {
            JsonNode authDetails = objectMapper.readTree(config.getAuthDetailsJson());
            
            // Check that required fields are present
            boolean hasSecretKey = authDetails.has("secretKey") && 
                                  !authDetails.path("secretKey").asText().isEmpty();
            
            // Secret key must be at least 32 characters for HS256
            if (hasSecretKey) {
                String secretKey = authDetails.path("secretKey").asText();
                return secretKey.length() >= 32;
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("Invalid configuration for JWT authenticator: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getRequiredConfigurationDescription() {
        return "JWT Token authenticator requires JSON configuration with:\n" +
               "{\n" +
               "  \"secretKey\": \"your-secret-key-here\",           // Secret key for JWT validation (min 32 chars)\n" +
               "  \"issuer\": \"vendor-name\",                       // Expected issuer claim (optional)\n" +
               "  \"audience\": \"api-service\",                     // Expected audience claim (optional)\n" +
               "  \"algorithm\": \"HS256\",                          // JWT algorithm (default: HS256)\n" +
               "  \"expirationMinutes\": 60,                         // Token expiration in minutes (optional)\n" +
               "  \"validateExpiration\": true,                      // Whether to validate expiration (default: true)\n" +
               "  \"validateIssuer\": true,                          // Whether to validate issuer (default: false)\n" +
               "  \"validateAudience\": true                         // Whether to validate audience (default: false)\n" +
               "}\n" +
               "The secretKey field is required and must be at least 32 characters long.";
    }
    
    /**
     * Validates a JWT token using the vendor configuration.
     * 
     * @param token The JWT token to validate
     * @param authDetails The vendor's authentication configuration
     * @param vendorId The vendor ID for logging
     * @return Claims if token is valid, null otherwise
     */
    private Claims validateJwtToken(String token, JsonNode authDetails, String vendorId) {
        try {
            String secretKey = authDetails.path("secretKey").asText();
            String algorithm = authDetails.path("algorithm").asText("HS256");
            boolean validateExpiration = authDetails.path("validateExpiration").asBoolean(true);
            boolean validateIssuer = authDetails.path("validateIssuer").asBoolean(false);
            boolean validateAudience = authDetails.path("validateAudience").asBoolean(false);
            
            // Create secret key
            SecretKey key = getSigningKey(secretKey, algorithm);
            
            // Build JWT parser
            JwtParserBuilder parserBuilder = Jwts.parserBuilder()
                    .setSigningKey(key);
            
            // Add validation rules based on configuration
            if (validateIssuer && authDetails.has("issuer")) {
                parserBuilder.requireIssuer(authDetails.path("issuer").asText());
            }
            
            if (validateAudience && authDetails.has("audience")) {
                parserBuilder.requireAudience(authDetails.path("audience").asText());
            }
            
            JwtParser parser = parserBuilder.build();
            
            // Parse and validate token
            Jws<Claims> jws = parser.parseClaimsJws(token);
            Claims claims = jws.getBody();
            
            // Manual expiration check if needed
            if (validateExpiration && claims.getExpiration() != null) {
                if (claims.getExpiration().before(new Date())) {
                    throw new ExpiredJwtException(null, claims, "Token has expired");
                }
            }
            
            log.debug("JWT token validated successfully for vendor: {}", vendorId);
            return claims;
            
        } catch (Exception e) {
            log.warn("JWT token validation failed for vendor: {} - {}", vendorId, e.getMessage());
            throw e; // Re-throw to be handled by the calling method
        }
    }
    
    private Jws<Claims> parseToken(String token, String secretKey, String algorithm) {
        try {
            // Get the signing key
            Key key = getSigningKey(secretKey, algorithm);
            
            // Parse the token with the key
            return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
                
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            throw new AuthenticationException("Token has expired", "JWT_TOKEN_EXPIRED", e);
        } catch (MalformedJwtException | SecurityException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            throw new AuthenticationException("Invalid token format", "JWT_TOKEN_INVALID", e);
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            throw new AuthenticationException("Invalid token signature", "JWT_SIGNATURE_INVALID", e);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Error parsing JWT token: {}", e.getMessage());
            throw new AuthenticationException("Error processing token", "JWT_PROCESSING_ERROR", e);
        }
    }
    
    private SecretKey getSigningKey(String secretKey, String algorithm) {
        try {
            // For HMAC algorithms, use the secret key directly
            if (algorithm.startsWith("HS")) {
                byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
                return Keys.hmacShaKeyFor(keyBytes);
            } 
            // Handle other key types if needed
            throw new UnsupportedOperationException("Unsupported algorithm: " + algorithm);
        } catch (Exception e) {
            log.error("Error creating signing key", e);
            throw new AuthenticationException("Error creating signing key: " + e.getMessage(), 
                "JWT_KEY_ERROR", e);
        }
    }
    
    /**
     * Extracts token expiry from JWT claims.
     * 
     * @param claims JWT claims
     * @return LocalDateTime of token expiry, or null if not present
     */
    private LocalDateTime getTokenExpiry(Claims claims) {
        if (claims.getExpiration() != null) {
            return claims.getExpiration().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        }
        return null;
    }
    
    /**
     * Generates a new JWT token (utility method for future use).
     * This could be used if the service needs to generate tokens for vendors.
     * 
     * @param authDetails Vendor configuration
     * @param subject Token subject
     * @return Generated JWT token
     */
    public String generateJwtToken(JsonNode authDetails, String subject) {
        String secretKey = authDetails.path("secretKey").asText();
        String issuer = authDetails.path("issuer").asText();
        String audience = authDetails.path("audience").asText();
        int expirationMinutes = authDetails.path("expirationMinutes").asInt(60);
        
        return generateToken(subject, secretKey, "HS256", expirationMinutes, issuer, audience);
    }
    
    private String generateToken(String subject, String secretKey, String algorithm, long expirationMinutes, 
                               String issuer, String audience) {
        try {
            // Get the signing key
            Key key = getSigningKey(secretKey, algorithm);
            
            // Create the JWT token
            Instant now = Instant.now();
            Instant expiration = now.plus(expirationMinutes, ChronoUnit.MINUTES);
            
            // Create the JWT builder
            JwtBuilder builder = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(key);
                
            // Add optional claims
            if (issuer != null && !issuer.trim().isEmpty()) {
                builder.setIssuer(issuer);
            }
            
            if (audience != null && !audience.trim().isEmpty()) {
                builder.setAudience(audience);
            }
            
            // Build the token
            return builder.compact();
            
        } catch (Exception e) {
            log.error("Error generating JWT token", e);
            throw new AuthenticationException("Error generating token: " + e.getMessage(), 
                "JWT_TOKEN_GENERATION_ERROR", e);
        }
    }
}
