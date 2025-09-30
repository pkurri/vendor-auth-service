package com.vendorauth.mapper;

import com.vendorauth.entity.VendorConfig;
import com.vendorauth.enums.AuthType;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis mapper interface for VendorConfig entity operations.
 * Provides methods to query vendor configurations by various criteria.
 */
@Mapper
public interface VendorConfigMapper {
    
    /**
     * Find a vendor configuration by ID
     */
    @Select("SELECT * FROM vendor_configs WHERE id = #{id}")
    Optional<VendorConfig> findById(Long id);
    
    /**
     * Find a vendor configuration by vendor ID
     */
    @Select("SELECT * FROM vendor_configs WHERE vendor_id = #{vendorId}")
    Optional<VendorConfig> findByVendorId(String vendorId);
    
    /**
     * Find all vendor configurations
     */
    @Select("SELECT * FROM vendor_configs ORDER BY id")
    List<VendorConfig> findAll();
    
    /**
     * Find all active vendor configurations
     */
    @Select("SELECT * FROM vendor_configs WHERE active = 1 ORDER BY id")
    List<VendorConfig> findByActiveTrue();
    
    /**
     * Find all vendor configurations by authentication type
     */
    @Select("SELECT * FROM vendor_configs WHERE auth_type = #{authType} ORDER BY id")
    List<VendorConfig> findByAuthType(AuthType authType);
    
    /**
     * Find active vendor configurations by authentication type
     */
    @Select("SELECT * FROM vendor_configs WHERE auth_type = #{authType} AND active = 1 ORDER BY id")
    List<VendorConfig> findByAuthTypeAndActiveTrue(AuthType authType);
    
    /**
     * Check if a vendor ID already exists
     */
    @Select("SELECT COUNT(*) FROM vendor_configs WHERE vendor_id = #{vendorId}")
    boolean existsByVendorId(String vendorId);
    
    /**
     * Check if a vendor configuration exists by ID
     */
    @Select("SELECT COUNT(*) FROM vendor_configs WHERE id = #{id}")
    boolean existsById(Long id);
    
    /**
     * Find vendor configurations by vendor name (case-insensitive)
     */
    @Select("SELECT * FROM vendor_configs WHERE LOWER(vendor_name) LIKE LOWER(CONCAT('%', #{name}, '%')) ORDER BY id")
    List<VendorConfig> findByVendorNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Insert a new vendor configuration
     */
    @Insert("INSERT INTO vendor_configs (vendor_id, vendor_name, auth_type, auth_details_json, active, base_url, " +
            "timeout_seconds, max_retries, description, created_at, updated_at) " +
            "VALUES (#{vendorId}, #{vendorName}, #{authType}, #{authDetailsJson}, #{active}, #{baseUrl}, " +
            "#{timeoutSeconds}, #{maxRetries}, #{description}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(VendorConfig vendorConfig);
    
    /**
     * Update an existing vendor configuration
     */
    @Update("UPDATE vendor_configs SET vendor_id = #{vendorId}, vendor_name = #{vendorName}, " +
            "auth_type = #{authType}, auth_details_json = #{authDetailsJson}, active = #{active}, " +
            "base_url = #{baseUrl}, timeout_seconds = #{timeoutSeconds}, max_retries = #{maxRetries}, " +
            "description = #{description}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(VendorConfig vendorConfig);
    
    /**
     * Delete a vendor configuration by ID
     */
    @Delete("DELETE FROM vendor_configs WHERE id = #{id}")
    int deleteById(Long id);
    
    /**
     * Count all vendor configurations
     */
    @Select("SELECT COUNT(*) FROM vendor_configs")
    long count();
}
