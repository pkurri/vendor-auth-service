package com.vendorauth.enums;

/**
 * Enumeration of supported authentication types for external vendors.
 * This enum allows for easy extension when new authentication mechanisms are added.
 */
public enum AuthType {
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
    BASIC,
    
    /**
     * Custom authentication mechanism specific to a vendor
     */
    CUSTOM;

    /**
     * Case-insensitive parser that accepts common separators (dash/underscore/space)
     * and returns CUSTOM for unknown values.
     */
    public static AuthType fromString(String value) {
        if (value == null) return CUSTOM;
        String normalized = value.trim().toUpperCase()
                .replace('-', '_')
                .replace(' ', '_');
        switch (normalized) {
            case "OAUTH2":
                return OAUTH2;
            case "API_KEY":
                return API_KEY;
            case "BASIC":
                return BASIC;
            case "CUSTOM":
                return CUSTOM;
            default:
                return CUSTOM;
        }
    }

    /**
     * Human-friendly display name for each auth type.
     */
    public String getDisplayName() {
        switch (this) {
            case OAUTH2:
                return "OAuth 2.0";
            case API_KEY:
                return "API Key";
            case BASIC:
                return "Basic Auth";
            case CUSTOM:
            default:
                return "Custom";
        }
    }

    /**
     * Validates whether the provided string maps to a supported auth type.
     */
    public static boolean isValidAuthType(String value) {
        if (value == null || value.trim().isEmpty()) return false;
        // fromString returns CUSTOM for unknown values; consider only known inputs as valid
        String normalized = value.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        return normalized.equals("OAUTH2") || normalized.equals("API_KEY") || normalized.equals("BASIC") || normalized.equals("CUSTOM");
    }
}
