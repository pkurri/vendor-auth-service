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
import org.springframework.util.Assert;

import java.util.Collections;

/**
 * Utility class for JWT testing.
 * Provides methods to generate test tokens and authentication objects.
 */
@TestComponent
public class TestJwtUtils {

    public static final String DEFAULT_TEST_PASSWORD = "test-password-123";
    public static final String ROLE_PREFIX = "ROLE_";
    
    private final JwtTokenProvider tokenProvider;

    @Autowired
    public TestJwtUtils(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    /**
     * Generate a JWT token for testing
     * @param username the username (required)
     * @param role the user role (without ROLE_ prefix)
     * @return JWT token as string
     * @throws IllegalArgumentException if username or role is null or empty
     */
    public String generateTestToken(String username, String role) {
        Assert.hasText(username, "Username must not be null or empty");
        Assert.hasText(role, "Role must not be null or empty");
        
        UserDetails userDetails = createTestUserDetails(username, role);
        return tokenProvider.generateToken(userDetails);
    }

    /**
     * Create an authentication object for testing
     * @param username the username (required)
     * @param role the user role (without ROLE_ prefix)
     * @return Authentication object
     * @throws IllegalArgumentException if username or role is null or empty
     */
    public Authentication createTestAuthentication(String username, String role) {
        Assert.hasText(username, "Username must not be null or empty");
        Assert.hasText(role, "Role must not be null or empty");
        
        UserDetails userDetails = createTestUserDetails(username, role);
        return new UsernamePasswordAuthenticationToken(
                userDetails, 
                null, 
                userDetails.getAuthorities()
        );
    }

    /**
     * Create an authentication header with a test token
     * @param username the username (required)
     * @param role the user role (without ROLE_ prefix)
     * @return HttpHeaders with Authorization header
     * @throws IllegalArgumentException if username or role is null or empty
     */
    public HttpHeaders authHeader(String username, String role) {
        String token = generateTestToken(username, role);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    /**
     * Create a test UserDetails object
     * @param username the username
     * @param role the role (without ROLE_ prefix)
     * @return UserDetails object
     */
    private UserDetails createTestUserDetails(String username, String role) {
        String roleWithPrefix = role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role;
        
        return User.builder()
                .username(username)
                .password(DEFAULT_TEST_PASSWORD)
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority(roleWithPrefix)))
                .build();
    }
}
