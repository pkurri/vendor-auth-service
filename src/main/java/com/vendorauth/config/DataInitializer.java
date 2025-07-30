package com.vendorauth.config;

import com.vendorauth.entity.VendorConfig;
import com.vendorauth.enums.AuthType;
import com.vendorauth.repository.VendorConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Data initializer to populate sample vendor configurations for development and testing.
 * This component runs at application startup and creates sample vendor configs.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final VendorConfigRepository vendorConfigRepository;
    
    @Override
    public void run(String... args) {
        if (vendorConfigRepository.count() == 0) {
            log.info("Initializing sample vendor configurations...");
            createSampleVendorConfigs();
            log.info("Sample vendor configurations created successfully");
        } else {
            log.info("Vendor configurations already exist, skipping initialization");
        }
    }
    
    private void createSampleVendorConfigs() {
        // Sample NoOp vendor for testing
        VendorConfig noopVendor = VendorConfig.builder()
                .vendorId("test-vendor")
                .vendorName("Test Vendor (NoOp)")
                .authType(AuthType.NOOP)
                .authDetailsJson("{\"description\": \"No-operation test vendor\"}")
                .baseUrl("https://api.test-vendor.com")
                .description("Test vendor using NoOp authenticator for development")
                .active(true)
                .build();
        vendorConfigRepository.save(noopVendor);
        
        // Sample OAuth2 vendor configuration (placeholder)
        VendorConfig oauth2Vendor = VendorConfig.builder()
                .vendorId("oauth2-vendor")
                .vendorName("OAuth2 Sample Vendor")
                .authType(AuthType.OAUTH2)
                .authDetailsJson("{\n" +
                        "  \"clientId\": \"sample_client_id\",\n" +
                        "  \"clientSecret\": \"sample_client_secret\",\n" +
                        "  \"authUrl\": \"https://api.oauth2vendor.com/oauth/authorize\",\n" +
                        "  \"tokenUrl\": \"https://api.oauth2vendor.com/oauth/token\",\n" +
                        "  \"scope\": \"read write\"\n" +
                        "}")
                .baseUrl("https://api.oauth2vendor.com")
                .description("Sample OAuth2 vendor configuration")
                .active(false) // Inactive until OAuth2Authenticator is implemented
                .build();
        vendorConfigRepository.save(oauth2Vendor);
        
        // Sample API Key vendor configuration (placeholder)
        VendorConfig apiKeyVendor = VendorConfig.builder()
                .vendorId("apikey-vendor")
                .vendorName("API Key Sample Vendor")
                .authType(AuthType.API_KEY)
                .authDetailsJson("{\n" +
                        "  \"apiKeyHeader\": \"X-API-Key\",\n" +
                        "  \"apiKeyQueryParam\": \"api_key\",\n" +
                        "  \"authMethod\": \"header\"\n" +
                        "}")
                .baseUrl("https://api.apikeyvendor.com")
                .description("Sample API Key vendor configuration")
                .active(false) // Inactive until ApiKeyAuthenticator is implemented
                .build();
        vendorConfigRepository.save(apiKeyVendor);
        
        // Sample Basic Auth vendor configuration (placeholder)
        VendorConfig basicAuthVendor = VendorConfig.builder()
                .vendorId("basic-auth-vendor")
                .vendorName("Basic Auth Sample Vendor")
                .authType(AuthType.BASIC_AUTH)
                .authDetailsJson("{\n" +
                        "  \"realm\": \"VendorAPI\",\n" +
                        "  \"encoding\": \"UTF-8\"\n" +
                        "}")
                .baseUrl("https://api.basicauthvendor.com")
                .description("Sample Basic Auth vendor configuration")
                .active(false) // Inactive until BasicAuthAuthenticator is implemented
                .build();
        vendorConfigRepository.save(basicAuthVendor);
        
        // Sample JWT Token vendor configuration
        VendorConfig jwtVendor = VendorConfig.builder()
                .vendorId("jwt-vendor")
                .vendorName("JWT Token Sample Vendor")
                .authType(AuthType.JWT_TOKEN)
                .authDetailsJson("{\n" +
                        "  \"secretKey\": \"my-super-secret-jwt-key-that-is-at-least-32-characters-long\",\n" +
                        "  \"issuer\": \"jwt-vendor\",\n" +
                        "  \"audience\": \"vendor-auth-service\",\n" +
                        "  \"algorithm\": \"HS256\",\n" +
                        "  \"expirationMinutes\": 60,\n" +
                        "  \"validateExpiration\": true,\n" +
                        "  \"validateIssuer\": true,\n" +
                        "  \"validateAudience\": true\n" +
                        "}")
                .baseUrl("https://api.jwtvendor.com")
                .description("Sample JWT Token vendor configuration")
                .active(true) // Active since JwtTokenAuthenticator is implemented
                .build();
        vendorConfigRepository.save(jwtVendor);
    }
}
