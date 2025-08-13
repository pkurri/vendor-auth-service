package com.vendorauth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vendorauth.config.TestSecurityConfig;
import com.vendorauth.dto.JwtAuthenticationRequest;
import com.vendorauth.dto.JwtAuthenticationResponse;
import com.vendorauth.dto.TokenRefreshRequest;
import com.vendorauth.security.JwtTokenProvider;
import com.vendorauth.util.TestJwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestSecurityConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @MockBean
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        // Setup mockMvc with the web application context
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        // Mock authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken("testuser", null);
        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);
    }

    @Test
    void authenticateUser_WithValidCredentials_ShouldReturnTokens() throws Exception {
        // Arrange
        JwtAuthenticationRequest authRequest = new JwtAuthenticationRequest("testuser", "password");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    void refreshToken_WithValidRefreshToken_ShouldReturnNewTokens() throws Exception {
        // Arrange
        String refreshToken = tokenProvider.generateRefreshToken("testuser");
        TokenRefreshRequest request = new TokenRefreshRequest(refreshToken);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    void refreshToken_WithInvalidRefreshToken_ShouldReturnBadRequest() throws Exception {
        // Arrange
        TokenRefreshRequest request = new TokenRefreshRequest("invalid.refresh.token");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() throws Exception {
        // Arrange
        String token = tokenProvider.generateToken("testuser");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/validate")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(token))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/validate")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("invalid.token.here"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
