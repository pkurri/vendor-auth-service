package com.vendorauth.repository;

import com.vendorauth.entity.VendorConfig;
import com.vendorauth.enums.AuthType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for VendorConfig entity operations.
 * Provides methods to query vendor configurations by various criteria.
 */
@Repository
public interface VendorConfigRepository extends JpaRepository<VendorConfig, Long> {
    
    /**
     * Find a vendor configuration by vendor ID
     */
    Optional<VendorConfig> findByVendorId(String vendorId);
    
    /**
     * Find all active vendor configurations
     */
    List<VendorConfig> findByActiveTrue();
    
    /**
     * Find all vendor configurations by authentication type
     */
    List<VendorConfig> findByAuthType(AuthType authType);
    
    /**
     * Find active vendor configurations by authentication type
     */
    List<VendorConfig> findByAuthTypeAndActiveTrue(AuthType authType);
    
    /**
     * Check if a vendor ID already exists
     */
    boolean existsByVendorId(String vendorId);
    
    /**
     * Find vendor configurations by vendor name (case-insensitive)
     */
    @Query("SELECT v FROM VendorConfig v WHERE LOWER(v.vendorName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<VendorConfig> findByVendorNameContainingIgnoreCase(@Param("name") String name);
}
