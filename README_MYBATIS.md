# MyBatis Integration - Developer Guide

## Overview

This project uses MyBatis 3.0.3 as the persistence framework, replacing JPA/Hibernate for better SQL control and performance.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Service Layer                            │
│              (VendorAuthenticationService)                   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  Repository Layer                            │
│              (VendorConfigRepository)                        │
│         - Wraps MyBatis Mapper                              │
│         - Provides JPA-like interface                        │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   Mapper Layer                               │
│              (VendorConfigMapper)                            │
│         - MyBatis interface with @Mapper                     │
│         - SQL queries with annotations                       │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   Database Layer                             │
│                  (SQL Server)                                │
└─────────────────────────────────────────────────────────────┘
```

## Key Components

### 1. Mapper Interface

**Location**: `src/main/java/com/vendorauth/mapper/VendorConfigMapper.java`

```java
@Mapper
public interface VendorConfigMapper {
    
    @Select("SELECT * FROM vendor_configs WHERE id = #{id}")
    Optional<VendorConfig> findById(Long id);
    
    @Insert("INSERT INTO vendor_configs (...) VALUES (...)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(VendorConfig vendorConfig);
    
    @Update("UPDATE vendor_configs SET ... WHERE id = #{id}")
    int update(VendorConfig vendorConfig);
    
    @Delete("DELETE FROM vendor_configs WHERE id = #{id}")
    int deleteById(Long id);
}
```

**Features**:
- Annotation-based SQL queries
- Type-safe method signatures
- Automatic parameter mapping
- Support for complex queries

### 2. Repository Wrapper

**Location**: `src/main/java/com/vendorauth/repository/VendorConfigRepository.java`

```java
@Repository
@RequiredArgsConstructor
public class VendorConfigRepository {
    
    private final VendorConfigMapper mapper;
    
    public Optional<VendorConfig> findById(Long id) {
        return mapper.findById(id);
    }
    
    public VendorConfig save(VendorConfig vendorConfig) {
        if (vendorConfig.getId() == null) {
            mapper.insert(vendorConfig);
        } else {
            mapper.update(vendorConfig);
        }
        return vendorConfig;
    }
}
```

**Purpose**:
- Maintains backward compatibility with JPA-style interface
- Handles insert vs update logic
- Manages timestamps
- No changes needed in service layer

### 3. Entity Class

**Location**: `src/main/java/com/vendorauth/entity/VendorConfig.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorConfig {
    private Long id;
    
    @NotBlank
    private String vendorId;
    
    @NotBlank
    private String vendorName;
    
    @NotNull
    private AuthType authType;
    
    private String authDetailsJson;
    private Boolean active;
    private String baseUrl;
    private Integer timeoutSeconds;
    private Integer maxRetries;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**Changes from JPA**:
- Removed `@Entity`, `@Table`, `@Column` annotations
- Kept validation annotations (`@NotBlank`, `@NotNull`)
- Now a simple POJO with Lombok

### 4. Type Handler

**Location**: `src/main/java/com/vendorauth/mybatis/typehandler/AuthTypeHandler.java`

```java
@MappedTypes(AuthType.class)
public class AuthTypeHandler extends BaseTypeHandler<AuthType> {
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, 
                                    AuthType parameter, JdbcType jdbcType) {
        ps.setString(i, parameter.name());
    }
    
    @Override
    public AuthType getNullableResult(ResultSet rs, String columnName) {
        String value = rs.getString(columnName);
        return value == null ? null : AuthType.valueOf(value);
    }
}
```

**Purpose**:
- Converts between Java enum and database VARCHAR
- Automatic registration via package scan
- Handles null values properly

## Configuration

### application.yml

```yaml
mybatis:
  # Location of XML mapper files (if using XML)
  mapper-locations: classpath:mapper/**/*.xml
  
  # Package for entity type aliases
  type-aliases-package: com.vendorauth.entity
  
  configuration:
    # Auto-convert snake_case to camelCase
    map-underscore-to-camel-case: true
    
    # Default fetch size for queries
    default-fetch-size: 100
    
    # Query timeout in seconds
    default-statement-timeout: 30
    
    # Logging implementation
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  
  # Package for custom type handlers
  type-handlers-package: com.vendorauth.mybatis.typehandler
```

### Main Application Class

```java
@SpringBootApplication
@MapperScan("com.vendorauth.mapper")
public class VendorAuthenticationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VendorAuthenticationServiceApplication.class, args);
    }
}
```

**Key**: `@MapperScan` annotation enables automatic mapper discovery

## Usage Examples

### Basic CRUD Operations

```java
@Service
@RequiredArgsConstructor
public class VendorService {
    
    private final VendorConfigRepository repository;
    
    // Create
    public VendorConfig createVendor(VendorConfig vendor) {
        return repository.save(vendor);
    }
    
    // Read
    public Optional<VendorConfig> getVendor(Long id) {
        return repository.findById(id);
    }
    
    // Update
    public VendorConfig updateVendor(VendorConfig vendor) {
        return repository.save(vendor);
    }
    
    // Delete
    public void deleteVendor(Long id) {
        repository.deleteById(id);
    }
    
    // Query
    public List<VendorConfig> getActiveVendors() {
        return repository.findByActiveTrue();
    }
}
```

### Custom Queries

Add to mapper interface:

```java
@Mapper
public interface VendorConfigMapper {
    
    // Simple query
    @Select("SELECT * FROM vendor_configs WHERE vendor_name = #{name}")
    List<VendorConfig> findByName(String name);
    
    // Query with multiple parameters
    @Select("SELECT * FROM vendor_configs " +
            "WHERE auth_type = #{authType} AND active = #{active}")
    List<VendorConfig> findByAuthTypeAndActive(
        @Param("authType") AuthType authType,
        @Param("active") Boolean active
    );
    
    // Query with LIKE
    @Select("SELECT * FROM vendor_configs " +
            "WHERE vendor_name LIKE CONCAT('%', #{keyword}, '%')")
    List<VendorConfig> searchByKeyword(@Param("keyword") String keyword);
    
    // Count query
    @Select("SELECT COUNT(*) FROM vendor_configs WHERE active = 1")
    long countActive();
    
    // Complex query with JOIN (example)
    @Select("SELECT vc.*, vl.* FROM vendor_configs vc " +
            "LEFT JOIN vendor_logs vl ON vc.id = vl.vendor_id " +
            "WHERE vc.id = #{id}")
    VendorConfigWithLogs findWithLogs(Long id);
}
```

### Dynamic SQL with XML Mappers

For complex dynamic queries, use XML mappers:

**File**: `src/main/resources/mapper/VendorConfigMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.vendorauth.mapper.VendorConfigMapper">
    
    <!-- Dynamic search query -->
    <select id="searchVendors" resultType="VendorConfig">
        SELECT * FROM vendor_configs
        <where>
            <if test="vendorId != null">
                AND vendor_id = #{vendorId}
            </if>
            <if test="authType != null">
                AND auth_type = #{authType}
            </if>
            <if test="active != null">
                AND active = #{active}
            </if>
            <if test="keyword != null">
                AND (vendor_name LIKE CONCAT('%', #{keyword}, '%')
                     OR description LIKE CONCAT('%', #{keyword}, '%'))
            </if>
        </where>
        ORDER BY id
    </select>
    
    <!-- Batch insert -->
    <insert id="batchInsert" parameterType="list">
        INSERT INTO vendor_configs 
        (vendor_id, vendor_name, auth_type, active, created_at, updated_at)
        VALUES
        <foreach collection="list" item="item" separator=",">
            (#{item.vendorId}, #{item.vendorName}, #{item.authType}, 
             #{item.active}, #{item.createdAt}, #{item.updatedAt})
        </foreach>
    </insert>
    
</mapper>
```

Add method to mapper interface:

```java
List<VendorConfig> searchVendors(
    @Param("vendorId") String vendorId,
    @Param("authType") AuthType authType,
    @Param("active") Boolean active,
    @Param("keyword") String keyword
);

int batchInsert(@Param("list") List<VendorConfig> vendors);
```

## Testing

### Unit Test Example

```java
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class VendorConfigMapperTest {
    
    @Autowired
    private VendorConfigMapper mapper;
    
    @Test
    void testInsertAndFind() {
        // Given
        VendorConfig vendor = VendorConfig.builder()
            .vendorId("test-vendor")
            .vendorName("Test Vendor")
            .authType(AuthType.API_KEY)
            .active(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        // When
        mapper.insert(vendor);
        Optional<VendorConfig> found = mapper.findById(vendor.getId());
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getVendorId()).isEqualTo("test-vendor");
    }
}
```

### Integration Test Example

```java
@SpringBootTest
@ActiveProfiles("test")
class VendorServiceIntegrationTest {
    
    @Autowired
    private VendorConfigRepository repository;
    
    @Test
    void testFullCrudCycle() {
        // Create
        VendorConfig vendor = VendorConfig.builder()
            .vendorId("integration-test")
            .vendorName("Integration Test Vendor")
            .authType(AuthType.OAUTH2)
            .active(true)
            .build();
        
        VendorConfig saved = repository.save(vendor);
        assertThat(saved.getId()).isNotNull();
        
        // Read
        Optional<VendorConfig> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        
        // Update
        found.get().setActive(false);
        repository.save(found.get());
        
        // Verify update
        Optional<VendorConfig> updated = repository.findById(saved.getId());
        assertThat(updated.get().getActive()).isFalse();
        
        // Delete
        repository.deleteById(saved.getId());
        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}
```

## Best Practices

### 1. Use Parameterized Queries

✅ **Good**:
```java
@Select("SELECT * FROM vendor_configs WHERE vendor_id = #{vendorId}")
Optional<VendorConfig> findByVendorId(String vendorId);
```

❌ **Bad** (SQL Injection risk):
```java
@Select("SELECT * FROM vendor_configs WHERE vendor_id = '${vendorId}'")
Optional<VendorConfig> findByVendorId(String vendorId);
```

### 2. Handle Null Values

```java
@Select("SELECT * FROM vendor_configs WHERE " +
        "(@{authType} IS NULL OR auth_type = #{authType})")
List<VendorConfig> findByOptionalAuthType(@Param("authType") AuthType authType);
```

### 3. Use Result Maps for Complex Mappings

```xml
<resultMap id="vendorConfigMap" type="VendorConfig">
    <id property="id" column="id"/>
    <result property="vendorId" column="vendor_id"/>
    <result property="vendorName" column="vendor_name"/>
    <result property="authType" column="auth_type" 
            typeHandler="com.vendorauth.mybatis.typehandler.AuthTypeHandler"/>
</resultMap>
```

### 4. Enable Logging for Development

```yaml
logging:
  level:
    com.vendorauth.mapper: DEBUG
    org.mybatis: DEBUG
```

### 5. Use Transactions

```java
@Service
@Transactional
public class VendorService {
    
    @Transactional
    public void updateMultipleVendors(List<VendorConfig> vendors) {
        vendors.forEach(repository::save);
        // All saves in one transaction
    }
}
```

## Performance Optimization

### 1. Batch Operations

```java
@Mapper
public interface VendorConfigMapper {
    
    @Insert("<script>" +
            "INSERT INTO vendor_configs (vendor_id, vendor_name, auth_type) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.vendorId}, #{item.vendorName}, #{item.authType})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("list") List<VendorConfig> vendors);
}
```

### 2. Pagination

```java
@Select("SELECT * FROM vendor_configs " +
        "ORDER BY id " +
        "OFFSET #{offset} ROWS FETCH NEXT #{limit} ROWS ONLY")
List<VendorConfig> findWithPagination(
    @Param("offset") int offset,
    @Param("limit") int limit
);
```

### 3. Lazy Loading

```java
@Select("SELECT id, vendor_id, vendor_name FROM vendor_configs")
List<VendorConfig> findAllLazy(); // Only load essential fields
```

### 4. Connection Pooling

Already configured with HikariCP in `application.yml`:

```yaml
spring:
  datasource:
    hikari:
      connection-timeout: 20000
      maximum-pool-size: 10
      minimum-idle: 5
      idle-timeout: 300000
```

## Troubleshooting

### Issue: Mapper Not Found

**Error**: `org.apache.ibatis.binding.BindingException: Invalid bound statement`

**Solution**:
1. Check `@MapperScan` annotation in main class
2. Verify mapper interface has `@Mapper` annotation
3. Ensure namespace in XML matches mapper interface fully qualified name

### Issue: Type Conversion Error

**Error**: `org.apache.ibatis.type.TypeException`

**Solution**:
1. Create custom type handler
2. Register in `application.yml`
3. Use `@MappedTypes` annotation

### Issue: SQL Syntax Error

**Error**: SQL syntax exception

**Solution**:
1. Enable SQL logging
2. Test query directly in database
3. Check SQL Server vs H2 syntax differences
4. Verify parameter names match

## Migration from JPA

If you're migrating existing JPA code:

1. **Remove JPA annotations** from entities
2. **Create mapper interface** with SQL queries
3. **Update repository** to use mapper
4. **Add type handlers** for enums and custom types
5. **Update tests** to use `@MybatisTest`
6. **Update configuration** in `application.yml`

See `MYBATIS_MIGRATION.md` for detailed migration guide.

## Resources

- **MyBatis Documentation**: https://mybatis.org/mybatis-3/
- **MyBatis Spring Boot**: https://mybatis.org/spring-boot-starter/
- **SQL Server JDBC**: https://docs.microsoft.com/en-us/sql/connect/jdbc/

## Summary

MyBatis provides:
- ✅ Direct SQL control
- ✅ Better performance
- ✅ Easier debugging
- ✅ Flexibility for complex queries
- ✅ Database-specific feature support

Perfect for applications requiring fine-grained SQL control and optimal performance.
