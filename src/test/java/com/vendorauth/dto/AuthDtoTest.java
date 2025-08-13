package com.vendorauth.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthDtoTest {

    @Test
    void testJwtAuthenticationRequest() {
        // Test constructor and getters
        JwtAuthenticationRequest request = new JwtAuthenticationRequest("testuser", "password");
        assertEquals("testuser", request.getUsername());
        assertEquals("password", request.getPassword());

        // Test setters
        request.setUsername("newuser");
        request.setPassword("newpass");
        assertEquals("newuser", request.getUsername());
        assertEquals("newpass", request.getPassword());

        // Test toString
        assertNotNull(request.toString());
    }

    @Test
    void testJwtAuthenticationResponse() {
        // Test constructor and getters
        JwtAuthenticationResponse response = new JwtAuthenticationResponse("token", "refreshToken", 3600L);
        assertEquals("token", response.getToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals(3600L, response.getExpiresIn());

        // Test setters
        response.setToken("newToken");
        response.setRefreshToken("newRefreshToken");
        response.setExpiresIn(7200L);
        
        assertEquals("newToken", response.getToken());
        assertEquals("newRefreshToken", response.getRefreshToken());
        assertEquals(7200L, response.getExpiresIn());

        // Test builder
        JwtAuthenticationResponse builtResponse = JwtAuthenticationResponse.builder()
                .token("builtToken")
                .refreshToken("builtRefreshToken")
                .expiresIn(10800L)
                .build();
                
        assertEquals("builtToken", builtResponse.getToken());
        assertEquals("builtRefreshToken", builtResponse.getRefreshToken());
        assertEquals(10800L, builtResponse.getExpiresIn());

        // Test toString
        assertNotNull(response.toString());
    }

    @Test
    void testTokenRefreshRequest() {
        // Test constructor and getter
        TokenRefreshRequest request = new TokenRefreshRequest("refreshToken");
        assertEquals("refreshToken", request.getRefreshToken());

        // Test setter
        request.setRefreshToken("newRefreshToken");
        assertEquals("newRefreshToken", request.getRefreshToken());

        // Test builder
        TokenRefreshRequest builtRequest = TokenRefreshRequest.builder()
                .refreshToken("builtRefreshToken")
                .build();
        assertEquals("builtRefreshToken", builtRequest.getRefreshToken());

        // Test toString
        assertNotNull(request.toString());
    }
}
