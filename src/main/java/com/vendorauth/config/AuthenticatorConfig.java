package com.vendorauth.config;

import com.vendorauth.enums.AuthType;
import com.vendorauth.service.VendorAuthenticator;
import com.vendorauth.service.impl.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for setting up the authenticator registry.
 * This class creates a map of AuthType to VendorAuthenticator implementations,
 * allowing the VendorAuthenticationService to dynamically select the correct authenticator.
 */
@Configuration
@RequiredArgsConstructor
public class AuthenticatorConfig {
    
    private final ApplicationContext applicationContext;
    
    /**
     * Creates a map of AuthType to VendorAuthenticator implementations.
     * This map is used by VendorAuthenticationService to select the appropriate
     * authenticator based on the vendor's configuration.
     * 
     * To add new authenticator types:
     * 1. Create a new implementation of VendorAuthenticator
     * 2. Add it as a Spring component (@Component)
     * 3. Add the mapping here
     * 
     * @return Map of AuthType to VendorAuthenticator
     */
    @Bean
    public Map<AuthType, VendorAuthenticator> authenticators() {
        Map<AuthType, VendorAuthenticator> authenticators = new HashMap<>();
        
        // Register API Key authenticator
        authenticators.put(AuthType.API_KEY, applicationContext.getBean(ApiKeyAuthenticator.class));
        
        // Register OAuth2 authenticator
        authenticators.put(AuthType.OAUTH2, applicationContext.getBean(OAuth2Authenticator.class));
        
        // Register Basic Auth authenticator
        authenticators.put(AuthType.BASIC, applicationContext.getBean(BasicAuthAuthenticator.class));

        // Map CUSTOM to JwtTokenAuthenticator to support JWT-based vendors under CUSTOM type
        authenticators.put(AuthType.CUSTOM, applicationContext.getBean(JwtTokenAuthenticator.class));
        
        return authenticators;
    }
}
