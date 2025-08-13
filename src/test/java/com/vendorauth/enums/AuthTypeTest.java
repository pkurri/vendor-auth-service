package com.vendorauth.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthTypeTest {

    @Test
    void testEnumValues() {
        // Test that all expected values exist
        assertEquals(4, AuthType.values().length);
        assertNotNull(AuthType.OAUTH2);
        assertNotNull(AuthType.API_KEY);
        assertNotNull(AuthType.BASIC);
        assertNotNull(AuthType.CUSTOM);

        // Test valueOf
        assertEquals(AuthType.OAUTH2, AuthType.valueOf("OAUTH2"));
        assertEquals(AuthType.API_KEY, AuthType.valueOf("API_KEY"));
        assertEquals(AuthType.BASIC, AuthType.valueOf("BASIC"));
        assertEquals(AuthType.CUSTOM, AuthType.valueOf("CUSTOM"));
    }

    @Test
    void testFromString() {
        // Test case-insensitive matching
        assertEquals(AuthType.OAUTH2, AuthType.fromString("oauth2"));
        assertEquals(AuthType.API_KEY, AuthType.fromString("api_key"));
        assertEquals(AuthType.BASIC, AuthType.fromString("basic"));
        assertEquals(AuthType.CUSTOM, AuthType.fromString("custom"));
        
        // Test with different cases
        assertEquals(AuthType.OAUTH2, AuthType.fromString("OAuth2"));
        assertEquals(AuthType.API_KEY, AuthType.fromString("API-KEY"));
        
        // Test with spaces
        assertEquals(AuthType.OAUTH2, AuthType.fromString(" oauth2 "));
        
        // Test default value for unknown type
        assertEquals(AuthType.CUSTOM, AuthType.fromString("unknown"));
    }

    @Test
    void testGetDisplayName() {
        assertEquals("OAuth 2.0", AuthType.OAUTH2.getDisplayName());
        assertEquals("API Key", AuthType.API_KEY.getDisplayName());
        assertEquals("Basic Auth", AuthType.BASIC.getDisplayName());
        assertEquals("Custom", AuthType.CUSTOM.getDisplayName());
    }

    @Test
    void testIsValidAuthType() {
        assertTrue(AuthType.isValidAuthType("oauth2"));
        assertTrue(AuthType.isValidAuthType("api_key"));
        assertTrue(AuthType.isValidAuthType("basic"));
        assertTrue(AuthType.isValidAuthType("custom"));
        
        assertFalse(AuthType.isValidAuthType("invalid"));
        assertFalse(AuthType.isValidAuthType(""));
        assertFalse(AuthType.isValidAuthType(null));
    }
}
