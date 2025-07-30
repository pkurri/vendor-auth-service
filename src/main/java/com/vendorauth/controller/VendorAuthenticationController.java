package com.vendorauth.controller;

import com.vendorauth.dto.AuthenticationRequest;
import com.vendorauth.dto.AuthenticationResponse;
import com.vendorauth.service.VendorAuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST controller for vendor authentication operations.
 * Provides endpoints for authenticating against external vendors.
 */
@RestController
@RequestMapping("/api/v1/authenticate")
@RequiredArgsConstructor
@Slf4j
public class VendorAuthenticationController {
    
    private final VendorAuthenticationService vendorAuthenticationService;
    
    /**
     * Authenticates against a specific vendor.
     * 
     * @param vendorId The unique identifier of the vendor
     * @param request The authentication request containing credentials
     * @return AuthenticationResponse with the result of the authentication attempt
     */
    @PostMapping("/vendor/{vendorId}")
    public ResponseEntity<AuthenticationResponse> authenticateVendor(
            @PathVariable String vendorId,
            @Valid @RequestBody AuthenticationRequest request) {
        
        log.info("Received authentication request for vendor: {}", vendorId);
        
        AuthenticationResponse response = vendorAuthenticationService.authenticate(vendorId, request);
        
        // Return appropriate HTTP status based on authentication result
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            // Return 401 for authentication failures, 400 for configuration issues
            if ("VENDOR_NOT_FOUND".equals(response.getErrorCode()) || 
                "VENDOR_INACTIVE".equals(response.getErrorCode()) ||
                "UNSUPPORTED_AUTH_TYPE".equals(response.getErrorCode()) ||
                "INVALID_CONFIG".equals(response.getErrorCode())) {
                return ResponseEntity.badRequest().body(response);
            } else {
                return ResponseEntity.status(401).body(response);
            }
        }
    }
    
    /**
     * Health check endpoint to verify the service is running.
     * 
     * @return Simple health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Vendor Authentication Service is running");
    }
    
    /**
     * Get vendor configuration status (for debugging/monitoring).
     * 
     * @param vendorId The unique identifier of the vendor
     * @return Status information about the vendor
     */
    @GetMapping("/vendor/{vendorId}/status")
    public ResponseEntity<String> getVendorStatus(@PathVariable String vendorId) {
        boolean isActive = vendorAuthenticationService.isVendorActive(vendorId);
        
        if (isActive) {
            return ResponseEntity.ok("Vendor " + vendorId + " is active and configured");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
