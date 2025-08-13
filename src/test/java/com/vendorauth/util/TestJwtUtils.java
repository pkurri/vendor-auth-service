package com.vendorauth.util;

import com.vendorauth.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

/**
 * Utility class for JWT testing.
 * Provides methods to generate test tokens and authentication objects.
 */
@TestComponent
public class TestJwtUtils {

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Generate a JWT token for testing
     * @param username the username
     * @param role the user role
     * @return JWT token as string
     */
    public String generateTestToken(String username, String role) {
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + role)))
                .build();

        return tokenProvider.generateToken(userDetails);
    }

    /**
     * Create an authentication object for testing
     * @param username the username
     * @param role the user role
     * @return Authentication object
     */
    public Authentication createTestAuthentication(String username, String role) {
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + role)))
                .build();

        return new UsernamePasswordAuthenticationToken(
                userDetails, 
                null, 
                userDetails.getAuthorities()
        );
    }

    /**
     * Create an authentication header with a test token
     * @param username the username
     * @param role the user role
     * @return HttpHeaders with Authorization header
     */
    public HttpHeaders authHeader(String username, String role) {
        String token = generateTestToken(username, role);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }
}
