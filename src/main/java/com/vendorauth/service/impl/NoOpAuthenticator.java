package com.vendorauth.service.impl;

import com.vendorauth.dto.AuthenticationRequest;
import com.vendorauth.dto.AuthenticationResponse;
import com.vendorauth.entity.VendorConfig;
import com.vendorauth.enums.AuthType;
import com.vendorauth.service.VendorAuthenticator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * No-operation authenticator for testing and placeholder purposes.
 * This implementation always returns a successful authentication response
 * without performing any actual authentication logic.
 * 
 * Useful for:
 * - Testing the authentication framework
 * - Placeholder during development
 * - Mock vendor integrations
 */
@Component
@Slf4j
public class NoOpAuthenticator implements VendorAuthenticator {
    
    @Override
    public AuthenticationResponse authenticate(VendorConfig config, AuthenticationRequest request) {
        log.info("NoOpAuthenticator: Performing no-op authentication for vendor: {}", config.getVendorId());
        
        // Simulate some processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return AuthenticationResponse.builder()
                .success(true)
                .vendorId(config.getVendorId())
                .message("No-op authentication successful")
                .accessToken("noop-token-" + System.currentTimeMillis())
                .build();
    }
    
    @Override
    public boolean isConfigurationValid(VendorConfig config) {
        // NoOp authenticator doesn't require any specific configuration
        return config != null && config.getAuthType() == AuthType.NOOP;
    }
    
    @Override
    public String getRequiredConfigurationDescription() {
        return "No configuration required for NoOp authenticator. " +
               "This is a placeholder authenticator that always returns success.";
    }
}
