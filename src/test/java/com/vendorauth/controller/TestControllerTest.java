package com.vendorauth.controller;

import com.vendorauth.config.TestSecurityConfig;
import com.vendorauth.util.TestJwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestSecurityConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TestControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private TestJwtUtils testJwtUtils;

    private MockMvc mockMvc;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Generate test tokens
        adminToken = testJwtUtils.generateTestToken("admin", "ADMIN");
        userToken = testJwtUtils.generateTestToken("user", "USER");
    }

    @Test
    void publicEndpoint_ShouldBeAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/test/public")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("This is a public endpoint. No authentication required."));
    }

    @Test
    void protectedEndpoint_WithValidToken_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/test/protected")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("This is a protected endpoint. You are authenticated."))
                .andExpect(jsonPath("$.username").value("user"));
    }

    @Test
    void protectedEndpoint_WithoutToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/test/protected")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoint_WithAdminRole_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/test/admin")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("This is an admin-only endpoint."));
    }

    @Test
    void adminEndpoint_WithUserRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/test/admin")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void userEndpoint_WithUserRole_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/test/user")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("This is a user-only endpoint."));
    }

    @Test
    void userEndpoint_WithAdminRole_ShouldBeAccessible() throws Exception {
        // Admin should have access to user endpoints as well
        mockMvc.perform(get("/api/v1/test/user")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("This is a user-only endpoint."));
    }
}
