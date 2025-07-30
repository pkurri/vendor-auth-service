package com.vendorauth.enums;

/**
 * Enumeration of supported authentication types for external vendors.
 * This enum allows for easy extension when new authentication mechanisms are added.
 */
public enum AuthType {
    /**
     * No-operation authenticator for testing and placeholder purposes
     */
    NOOP,
    
    /**
     * OAuth 2.0 authentication flow
     */
    OAUTH2,
    
    /**
     * Simple API key-based authentication
     */
    API_KEY,
    
    /**
     * Basic username/password authentication
     */
    BASIC_AUTH,
    
    /**
     * JWT token-based authentication
     */
    JWT_TOKEN,
    
    /**
     * Custom authentication mechanism specific to a vendor
     */
    CUSTOM
}
