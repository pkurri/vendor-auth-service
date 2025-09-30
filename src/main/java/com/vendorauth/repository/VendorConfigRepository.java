package com.vendorauth.repository;

import com.vendorauth.entity.VendorConfig;
import com.vendorauth.enums.AuthType;
import com.vendorauth.mapper.VendorConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for VendorConfig entity operations using MyBatis.
 * Provides methods to query vendor configurations by various criteria.
 * This class wraps the MyBatis mapper to provide a repository-style interface.
 */
@Repository
@RequiredArgsConstructor
public class VendorConfigRepository {
    
    private final VendorConfigMapper mapper;
    
    /**
     * Find a vendor configuration by ID
     */
    public Optional<VendorConfig> findById(Long id) {
        return mapper.findById(id);
    }
    
    /**
     * Find a vendor configuration by vendor ID
     */
    public Optional<VendorConfig> findByVendorId(String vendorId) {
        return mapper.findByVendorId(vendorId);
    }
    
    /**
     * Find all vendor configurations
     */
    public List<VendorConfig> findAll() {
        return mapper.findAll();
    }
    
    /**
     * Find all active vendor configurations
     */
    public List<VendorConfig> findByActiveTrue() {
        return mapper.findByActiveTrue();
    }
    
    /**
     * Find all vendor configurations by authentication type
     */
    public List<VendorConfig> findByAuthType(AuthType authType) {
        return mapper.findByAuthType(authType);
    }
    
    /**
     * Find active vendor configurations by authentication type
     */
    public List<VendorConfig> findByAuthTypeAndActiveTrue(AuthType authType) {
        return mapper.findByAuthTypeAndActiveTrue(authType);
    }
    
    /**
     * Check if a vendor ID already exists
     */
    public boolean existsByVendorId(String vendorId) {
        return mapper.existsByVendorId(vendorId);
    }
    
    /**
     * Check if a vendor configuration exists by ID
     */
    public boolean existsById(Long id) {
        return mapper.existsById(id);
    }
    
    /**
     * Find vendor configurations by vendor name (case-insensitive)
     */
    public List<VendorConfig> findByVendorNameContainingIgnoreCase(String name) {
        return mapper.findByVendorNameContainingIgnoreCase(name);
    }
    
    /**
     * Save (insert or update) a vendor configuration
     */
    public VendorConfig save(VendorConfig vendorConfig) {
        if (vendorConfig.getId() == null) {
            // Insert new record
            vendorConfig.setCreatedAt(LocalDateTime.now());
            vendorConfig.setUpdatedAt(LocalDateTime.now());
            mapper.insert(vendorConfig);
        } else {
            // Update existing record
            vendorConfig.setUpdatedAt(LocalDateTime.now());
            mapper.update(vendorConfig);
        }
        return vendorConfig;
    }
    
    /**
     * Delete a vendor configuration by ID
     */
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }
    
    /**
     * Count all vendor configurations
     */
    public long count() {
        return mapper.count();
    }
}
