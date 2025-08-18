package com.vendorauth.controller;

import com.vendorauth.dto.JwtAuthenticationRequest;
import com.vendorauth.dto.JwtAuthenticationResponse;
import com.vendorauth.dto.TokenRefreshRequest;
import com.vendorauth.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.Collections;

/**
 * Controller for JWT authentication endpoints
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Authenticate user and return JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody JwtAuthenticationRequest loginRequest) {
        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );

        // Set the authentication in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        String jwt = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);
        
        // Get token expiration time in seconds
        long expiresIn = tokenProvider.getExpirationDateFromToken(jwt).getTime() / 1000;

        return ResponseEntity.ok(new JwtAuthenticationResponse(
            jwt,
            refreshToken,
            expiresIn
        ));
    }

    /**
     * Refresh JWT token using refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        
        // Validate the refresh token
        if (!tokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.badRequest().body("Invalid refresh token");
        }
        
        // Get username from refresh token
        String username = tokenProvider.getUsernameFromToken(refreshToken);
        
        // Generate new access token
        String newToken = tokenProvider.generateAccessToken(username, null);
        
        // Generate new refresh token (optional: you might want to rotate refresh tokens)
        String newRefreshToken = tokenProvider.generateRefreshToken(username);
        
        // Get token expiration time in seconds
        long expiresIn = tokenProvider.getExpirationDateFromToken(newToken).getTime() / 1000;
        
        return ResponseEntity.ok(new JwtAuthenticationResponse(
            newToken,
            newRefreshToken,
            expiresIn
        ));
    }
    
    /**
     * Validate JWT token
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody String token) {
        boolean isValid = tokenProvider.validateToken(token);
        // Return plain boolean string to satisfy tests
        return ResponseEntity.ok(Boolean.toString(isValid));
    }
}
