package com.vendorauth.service;

import com.vendorauth.dto.AuthenticationRequest;
import com.vendorauth.dto.AuthenticationResponse;
import com.vendorauth.entity.VendorConfig;
import com.vendorauth.enums.AuthType;
import com.vendorauth.exception.AuthenticationException;
import com.vendorauth.repository.VendorConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.List;

/**
 * Core service for vendor authentication operations.
 * This service acts as a coordinator, selecting the appropriate authenticator
 * based on the vendor's configuration and delegating the actual authentication logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VendorAuthenticationService {
    
    private final VendorConfigRepository vendorConfigRepository;
    private final Map<AuthType, VendorAuthenticator> authenticators;
    
    /**
     * Authenticates against a specific vendor using the provided credentials.
     * 
     * @param vendorId The unique identifier of the vendor
     * @param request The authentication request containing credentials
     * @return AuthenticationResponse with the result of the authentication attempt
     * @throws AuthenticationException if the vendor is not found or configured
     */
    public AuthenticationResponse authenticate(String vendorId, AuthenticationRequest request) {
        log.info("Attempting authentication for vendor: {}", vendorId);
        
        try {
            // Find vendor configuration
            Optional<VendorConfig> configOpt = vendorConfigRepository.findByVendorId(vendorId);
            if (configOpt.isEmpty()) {
                log.warn("Vendor configuration not found for vendorId: {}", vendorId);
                return AuthenticationResponse.failure(
                    vendorId, 
                    "Vendor configuration not found", 
                    "VENDOR_NOT_FOUND"
                );
            }
            
            VendorConfig config = configOpt.get();
            
            // Check if vendor is active
            if (!config.getActive()) {
                log.warn("Vendor is inactive: {}", vendorId);
                return AuthenticationResponse.failure(
                    vendorId, 
                    "Vendor is currently inactive", 
                    "VENDOR_INACTIVE"
                );
            }
            
            // Get the appropriate authenticator
            VendorAuthenticator authenticator = authenticators.get(config.getAuthType());
            if (authenticator == null) {
                log.error("No authenticator found for auth type: {} (vendor: {})", 
                         config.getAuthType(), vendorId);
                return AuthenticationResponse.failure(
                    vendorId, 
                    "Authentication type not supported: " + config.getAuthType(), 
                    "UNSUPPORTED_AUTH_TYPE"
                );
            }
            
            // Validate configuration
            if (!authenticator.isConfigurationValid(config)) {
                log.error("Invalid configuration for vendor: {} (auth type: {})", 
                         vendorId, config.getAuthType());
                return AuthenticationResponse.failure(
                    vendorId, 
                    "Invalid vendor configuration", 
                    "INVALID_CONFIG"
                );
            }
            
            // Perform authentication
            log.debug("Using {} authenticator for vendor: {}", config.getAuthType(), vendorId);
            AuthenticationResponse response = authenticator.authenticate(config, request);
            
            log.info("Authentication completed for vendor: {} - Success: {}", 
                    vendorId, response.isSuccess());
            
            return response;
            
        } catch (Exception e) {
            log.error("Unexpected error during authentication for vendor: {}", vendorId, e);
            return AuthenticationResponse.failure(
                vendorId, 
                "Internal authentication error: " + e.getMessage(), 
                "INTERNAL_ERROR"
            );
        }
    }
    
    /**
     * Retrieves vendor configuration by vendor ID.
     * 
     * @param vendorId The unique identifier of the vendor
     * @return Optional containing the vendor configuration if found
     */
    public Optional<VendorConfig> getVendorConfig(String vendorId) {
        return vendorConfigRepository.findByVendorId(vendorId);
    }
    
    /**
     * Checks if a vendor is configured and active.
     * 
     * @param vendorId The unique identifier of the vendor
     * @return true if the vendor is configured and active
     */
    public boolean isVendorActive(String vendorId) {
        return vendorConfigRepository.findByVendorId(vendorId)
                .map(VendorConfig::getActive)
                .orElse(false);
    }
    
    /**
     * Gets the required configuration description for a specific authentication type.
     * 
     * @param authType The authentication type
     * @return Description of required configuration, or null if auth type not supported
     */
    public String getRequiredConfigurationDescription(AuthType authType) {
        VendorAuthenticator authenticator = authenticators.get(authType);
        return authenticator != null ? authenticator.getRequiredConfigurationDescription() : null;
    }

    // ------- Methods used by tests (simple repository delegations) -------
    public List<VendorConfig> getAllVendors() {
        return vendorConfigRepository.findAll();
    }

    public Optional<VendorConfig> getVendorById(Long id) {
        return vendorConfigRepository.findById(id);
    }

    public Optional<VendorConfig> getVendorByVendorId(String vendorId) {
        return vendorConfigRepository.findByVendorId(vendorId);
    }

    public VendorConfig createVendor(VendorConfig vendor) {
        return vendorConfigRepository.save(vendor);
    }

    public Optional<VendorConfig> updateVendor(Long id, VendorConfig vendor) {
        if (!vendorConfigRepository.existsById(id)) {
            return Optional.empty();
        }
        vendor.setId(id);
        return Optional.of(vendorConfigRepository.save(vendor));
    }

    public boolean deleteVendor(Long id) {
        if (!vendorConfigRepository.existsById(id)) {
            return false;
        }
        vendorConfigRepository.deleteById(id);
        return true;
    }

    public List<VendorConfig> getVendorsByAuthType(AuthType authType) {
        return vendorConfigRepository.findByAuthType(authType);
    }
}
