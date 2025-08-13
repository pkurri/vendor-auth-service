package com.vendorauth.config;

import com.vendorauth.security.JwtTokenProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Collections;

/**
 * Test configuration for security tests.
 * Provides in-memory user details service for testing.
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        // Create test users with different roles
        var adminUser = User.withUsername("admin")
                .password("{noop}admin123")  // {noop} for plain text password (only for testing)
                .roles("ADMIN")
                .build();

        var testUser = User.withUsername("user")
                .password("{noop}user123")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(adminUser, testUser);
    }

    @Bean
    @Primary
    public JwtTokenProvider jwtTokenProvider() {
        // Use a fixed secret key for testing
        JwtTokenProvider provider = new JwtTokenProvider();
        provider.setJwtSecret("test-secret-key-1234567890-1234567890-1234567890");
        return provider;
    }
}
