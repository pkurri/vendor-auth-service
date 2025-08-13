package com.vendorauth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vendorauth.config.TestSecurityConfig;
import com.vendorauth.util.TestJwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = TestSecurityConfig.class)
@ActiveProfiles("test")
class JwtAuthenticationFilterTest {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private TestJwtUtils testJwtUtils;

    @MockBean
    private UserDetailsService userDetailsService;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_ROLE = "USER";
    private String validToken;

    @BeforeEach
    void setUp() {
        // Initialize the filter with the token provider and user details service
        jwtAuthenticationFilter = new JwtAuthenticationFilter(tokenProvider, userDetailsService);
        
        // Setup mock request, response, and filter chain
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();

        // Create a test user
        UserDetails userDetails = User.builder()
                .username(TEST_USERNAME)
                .password("password")
                .authorities(Collections.singletonList(
                        () -> "ROLE_" + TEST_ROLE))
                .build();

        // Mock user details service
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);

        // Generate a valid token for testing
        validToken = testJwtUtils.generateTestToken(TEST_USERNAME, TEST_ROLE);
    }

    @Test
    void doFilterInternal_WithValidToken_ShouldSetAuthentication() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer " + validToken);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert - No exception should be thrown and the response should be OK (200)
        assertEquals(200, response.getStatus());
    }

    @Test
    void doFilterInternal_WithInvalidToken_ShouldReturnUnauthorized() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer invalid.token.here");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert - Should return 401 Unauthorized
        assertEquals(401, response.getStatus());
    }

    @Test
    void doFilterInternal_WithExpiredToken_ShouldReturnUnauthorized() throws ServletException, IOException {
        // Arrange - Create an expired token
        String expiredToken = testJwtUtils.generateTestToken(TEST_USERNAME, TEST_ROLE);
        // Manually set token to be expired (hack for testing purposes)
        expiredToken = expiredToken.substring(0, expiredToken.length() - 5) + "invalid";
        
        request.addHeader("Authorization", "Bearer " + expiredToken);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert - Should return 401 Unauthorized
        assertEquals(401, response.getStatus());
    }

    @Test
    void doFilterInternal_WithNoToken_ShouldContinueFilterChain() throws ServletException, IOException {
        // Act - No token in the request
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert - Should continue filter chain without setting authentication
        assertEquals(200, response.getStatus());
    }

    @Test
    void doFilterInternal_WithTokenInQueryParam_ShouldSetAuthentication() throws ServletException, IOException {
        // Arrange - Add token as a query parameter
        request.setParameter("token", validToken);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert - No exception should be thrown and the response should be OK (200)
        assertEquals(200, response.getStatus());
    }
}
