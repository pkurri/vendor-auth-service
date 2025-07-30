package com.vendorauth.entity;

import com.vendorauth.enums.AuthType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing vendor-specific authentication configuration.
 * This flexible design allows storing various authentication details as JSON
 * to accommodate unknown or diverse vendor requirements.
 */
@Entity
@Table(name = "vendor_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Unique identifier for the vendor
     */
    @Column(unique = true, nullable = false)
    @NotBlank(message = "Vendor ID cannot be blank")
    private String vendorId;
    
    /**
     * Human-readable vendor name
     */
    @Column(nullable = false)
    @NotBlank(message = "Vendor name cannot be blank")
    private String vendorName;
    
    /**
     * Type of authentication mechanism used by this vendor
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Auth type cannot be null")
    private AuthType authType;
    
    /**
     * Flexible JSON storage for vendor-specific authentication details.
     * This allows storing unknown authentication parameters without schema changes.
     * 
     * Example JSON structures:
     * - OAuth2: {"clientId": "xxx", "clientSecret": "yyy", "authUrl": "https://...", "tokenUrl": "https://..."}
     * - API Key: {"apiKeyHeader": "X-API-Key", "baseUrl": "https://api.vendor.com"}
     * - Basic Auth: {"baseUrl": "https://api.vendor.com", "realm": "VendorAPI"}
     */
    @Column(columnDefinition = "TEXT")
    private String authDetailsJson;
    
    /**
     * Whether this vendor configuration is currently active
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    /**
     * Base URL for the vendor's API (if applicable)
     */
    private String baseUrl;
    
    /**
     * Timeout in seconds for authentication requests to this vendor
     */
    @Builder.Default
    private Integer timeoutSeconds = 30;
    
    /**
     * Maximum number of retry attempts for failed authentication
     */
    @Builder.Default
    private Integer maxRetries = 3;
    
    /**
     * Additional notes or description about this vendor
     */
    @Column(length = 1000)
    private String description;
    
    /**
     * When this configuration was created
     */
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * When this configuration was last updated
     */
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
