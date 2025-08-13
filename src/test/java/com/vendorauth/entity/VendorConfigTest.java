package com.vendorauth.entity;

import com.vendorauth.enums.AuthType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class VendorConfigTest {

    private VendorConfig vendorConfig;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        vendorConfig = new VendorConfig();
        vendorConfig.setId(1L);
        vendorConfig.setVendorId("test-vendor-123");
        vendorConfig.setVendorName("Test Vendor");
        vendorConfig.setAuthType(AuthType.OAUTH2);
        vendorConfig.setActive(true);
        vendorConfig.setBaseUrl("https://api.testvendor.com");
        vendorConfig.setTimeout(5000);
        vendorConfig.setRetries(3);
        vendorConfig.setDescription("Test vendor configuration");
        vendorConfig.setAuthDetailsJson("{'clientId': 'test-client-id'}");
        vendorConfig.setCreatedAt(now);
        vendorConfig.setUpdatedAt(now);
    }

    @Test
    void testGettersAndSetters() {
        assertEquals(1L, vendorConfig.getId());
        assertEquals("test-vendor-123", vendorConfig.getVendorId());
        assertEquals("Test Vendor", vendorConfig.getVendorName());
        assertEquals(AuthType.OAUTH2, vendorConfig.getAuthType());
        assertTrue(vendorConfig.isActive());
        assertEquals("https://api.testvendor.com", vendorConfig.getBaseUrl());
        assertEquals(5000, vendorConfig.getTimeout());
        assertEquals(3, vendorConfig.getRetries());
        assertEquals("Test vendor configuration", vendorConfig.getDescription());
        assertEquals("{'clientId': 'test-client-id'}", vendorConfig.getAuthDetailsJson());
        assertEquals(now, vendorConfig.getCreatedAt());
        assertEquals(now, vendorConfig.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        VendorConfig sameVendor = new VendorConfig();
        sameVendor.setId(1L);
        sameVendor.setVendorId("test-vendor-123");

        VendorConfig differentVendor = new VendorConfig();
        differentVendor.setId(2L);
        differentVendor.setVendorId("different-vendor");

        // Test equals
        assertEquals(vendorConfig, sameVendor);
        assertNotEquals(vendorConfig, differentVendor);
        assertNotEquals(vendorConfig, null);
        assertNotEquals(vendorConfig, new Object());

        // Test hashCode
        assertEquals(vendorConfig.hashCode(), sameVendor.hashCode());
        assertNotEquals(vendorConfig.hashCode(), differentVendor.hashCode());
    }

    @Test
    void testToString() {
        String toString = vendorConfig.toString();
        assertTrue(toString.contains("Test Vendor"));
        assertTrue(toString.contains("test-vendor-123"));
        assertTrue(toString.contains("OAUTH2"));
    }

    @Test
    void testBuilder() {
        VendorConfig builtVendor = VendorConfig.builder()
                .id(2L)
                .vendorId("built-vendor")
                .vendorName("Built Vendor")
                .authType(AuthType.API_KEY)
                .active(false)
                .build();

        assertEquals(2L, builtVendor.getId());
        assertEquals("built-vendor", builtVendor.getVendorId());
        assertEquals("Built Vendor", builtVendor.getVendorName());
        assertEquals(AuthType.API_KEY, builtVendor.getAuthType());
        assertFalse(builtVendor.isActive());
    }
}
