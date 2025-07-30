package com.vendorauth.service;

import com.vendorauth.dto.AuthenticationRequest;
import com.vendorauth.dto.AuthenticationResponse;
import com.vendorauth.entity.VendorConfig;

/**
 * Core interface for vendor-specific authentication implementations.
 * This abstraction allows for easy addition of new authentication mechanisms
 * without modifying existing code.
 * 
 * Each concrete implementation should handle a specific authentication type
 * (OAuth2, API Key, Basic Auth, etc.) and know how to interpret the
 * vendor-specific configuration stored in VendorConfig.authDetailsJson.
 */
public interface VendorAuthenticator {
    
    /**
     * Authenticates against a specific vendor using the provided configuration and request.
     * 
     * @param config The vendor configuration containing authentication details
     * @param request The authentication request with credentials/tokens
     * @return AuthenticationResponse indicating success/failure and any returned tokens
     * @throws AuthenticationException if authentication fails due to technical issues
     */
    AuthenticationResponse authenticate(VendorConfig config, AuthenticationRequest request);
    
    /**
     * Validates that the provided vendor configuration contains all required
     * authentication details for this authenticator type.
     * 
     * @param config The vendor configuration to validate
     * @return true if the configuration is valid for this authenticator
     */
    boolean isConfigurationValid(VendorConfig config);
    
    /**
     * Returns a human-readable description of what authentication details
     * are required in the VendorConfig.authDetailsJson for this authenticator.
     * 
     * This is useful for documentation and configuration validation.
     * 
     * @return Description of required JSON structure
     */
    String getRequiredConfigurationDescription();
}
