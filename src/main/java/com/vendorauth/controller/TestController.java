package com.vendorauth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Test controller to verify JWT authentication and role-based access control
 */
@RestController
@RequestMapping("/api/v1/test")
@Tag(name = "Test", description = "Test endpoints for JWT authentication")
public class TestController {

    /**
     * Public endpoint - no authentication required
     */
    @GetMapping("/public")
    @Operation(summary = "Public endpoint - no authentication required")
    public ResponseEntity<Map<String, String>> publicEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This is a public endpoint. No authentication required.");
        return ResponseEntity.ok(response);
    }

    /**
     * Protected endpoint - requires authentication
     */
    @GetMapping("/protected")
    @Operation(
        summary = "Protected endpoint - requires authentication",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Map<String, Object>> protectedEndpoint(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is a protected endpoint. You are authenticated.");
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        return ResponseEntity.ok(response);
    }

    /**
     * Admin-only endpoint - requires ROLE_ADMIN
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
        summary = "Admin-only endpoint - requires ROLE_ADMIN",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Map<String, String>> adminEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This is an admin-only endpoint.");
        return ResponseEntity.ok(response);
    }

    /**
     * User-only endpoint - requires ROLE_USER
     */
    @GetMapping("/user")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(
        summary = "User-only endpoint - requires ROLE_USER",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Map<String, String>> userEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This is a user-only endpoint.");
        return ResponseEntity.ok(response);
    }
}
