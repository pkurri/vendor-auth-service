package com.vendorauth.security;

import com.vendorauth.config.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestSecurityConfig.class)
@ActiveProfiles("test")
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider tokenProvider;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_ROLE = "USER";
    private String testToken;

    @BeforeEach
    void setUp() {
        // Create a test user
        UserDetails userDetails = User.builder()
                .username(TEST_USERNAME)
                .password("password")
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + TEST_ROLE)))
                .build();

        // Generate a test token
        testToken = tokenProvider.generateToken(userDetails);
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        assertNotNull(testToken);
        assertTrue(testToken.length() > 0);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        assertTrue(tokenProvider.validateToken(testToken));
    }

    @Test
    void getUsernameFromToken_ShouldReturnCorrectUsername() {
        String username = tokenProvider.getUsernameFromToken(testToken);
        assertEquals(TEST_USERNAME, username);
    }

    @Test
    void getAuthentication_ShouldReturnValidAuthentication() {
        Authentication authentication = tokenProvider.getAuthentication(testToken);
        assertNotNull(authentication);
        assertEquals(TEST_USERNAME, authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + TEST_ROLE)));
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        String invalidToken = testToken + "invalid";
        assertFalse(tokenProvider.validateToken(invalidToken));
    }

    @Test
    void getExpirationDateFromToken_ShouldReturnFutureDate() {
        long expirationTime = tokenProvider.getExpirationDateFromToken(testToken).getTime();
        assertTrue(expirationTime > System.currentTimeMillis());
    }
}
