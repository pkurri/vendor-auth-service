package com.vendorauth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test to verify the Spring application context loads successfully
 * with MyBatis and OAuth2 configurations.
 */
@SpringBootTest
@ActiveProfiles("test")
class VendorAuthenticationServiceApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures that the Spring context loads successfully
        // with all the configured beans and dependencies including:
        // - MyBatis mappers
        // - OAuth2 Authorization Server
        // - Security configuration
        // - Repository beans
    }
}
