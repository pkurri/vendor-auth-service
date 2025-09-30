# MyBatis Migration Guide

## Overview

This document describes the migration from JPA/Hibernate to MyBatis (iBATIS successor) for database connectivity.

## Changes Made

### 1. Dependencies Updated

#### Removed (JPA/Hibernate):
```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.hibernate.orm:hibernate-core:6.4.4.Final'
implementation 'jakarta.persistence:jakarta.persistence-api:3.1.0'
```

#### Added (MyBatis):
```gradle
implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3'
testImplementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter-test:3.0.3'
testImplementation 'com.h2database:h2:2.2.224'
```

### 2. Configuration Changes

#### application.yml
Replaced JPA configuration with MyBatis:

```yaml
mybatis:
  mapper-locations: classpath:mapper/**/*.xml
  type-aliases-package: com.vendorauth.entity
  configuration:
    map-underscore-to-camel-case: true
    default-fetch-size: 100
    default-statement-timeout: 30
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  type-handlers-package: com.vendorauth.mybatis.typehandler
```

### 3. Entity Changes

#### VendorConfig.java
- Removed JPA annotations: `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@Enumerated`, `@Lob`, `@PreUpdate`
- Kept validation annotations: `@NotBlank`, `@NotNull`
- Now a simple POJO with Lombok annotations

### 4. Repository Changes

#### VendorConfigMapper.java (New)
- MyBatis mapper interface with `@Mapper` annotation
- Uses MyBatis annotations: `@Select`, `@Insert`, `@Update`, `@Delete`, `@Options`
- Direct SQL queries for all operations

#### VendorConfigRepository.java (Updated)
- Changed from `interface extends JpaRepository` to `@Repository class`
- Wraps `VendorConfigMapper` to provide repository-style interface
- Maintains backward compatibility with existing service code
- Handles `save()` logic (insert vs update based on ID)

### 5. Type Handlers

#### AuthTypeHandler.java (New)
- Custom MyBatis type handler for `AuthType` enum
- Handles conversion between enum and VARCHAR database column
- Located in `com.vendorauth.mybatis.typehandler` package

### 6. Application Class

#### VendorAuthenticationServiceApplication.java
- Added `@MapperScan("com.vendorauth.mapper")` annotation
- Enables automatic detection of MyBatis mapper interfaces

### 7. Test Changes

#### VendorConfigRepositoryTest.java
- Changed from `@DataJpaTest` to `@MybatisTest`
- Removed `TestEntityManager` dependency
- Uses repository's `save()` method directly for test data setup
- Added `@AutoConfigureTestDatabase` annotation

#### Test Configuration
- Created `application-test.yml` with H2 database configuration
- Created `schema.sql` for test database schema
- H2 configured in SQL Server compatibility mode

## Migration Benefits

### Advantages of MyBatis:

1. **Direct SQL Control**: Write and optimize SQL queries directly
2. **Performance**: No ORM overhead, better for complex queries
3. **Flexibility**: Easier to work with stored procedures and complex joins
4. **Debugging**: SQL queries are visible and easy to debug
5. **Learning Curve**: Simpler for developers familiar with SQL
6. **Database-Specific Features**: Easy to use SQL Server-specific features

### Trade-offs:

1. **More Boilerplate**: Need to write SQL for each operation
2. **No Automatic Schema Generation**: Must manage schema manually
3. **Type Safety**: Less compile-time checking compared to JPA Criteria API
4. **Relationship Mapping**: Manual handling of entity relationships

## Database Schema

The application uses the following table structure:

```sql
CREATE TABLE vendor_configs (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    vendor_id VARCHAR(255) NOT NULL UNIQUE,
    vendor_name VARCHAR(255) NOT NULL,
    auth_type VARCHAR(50) NOT NULL,
    auth_details_json NVARCHAR(MAX),
    active BIT NOT NULL DEFAULT 1,
    base_url VARCHAR(500),
    timeout_seconds INT DEFAULT 30,
    max_retries INT DEFAULT 3,
    description VARCHAR(1000),
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
);

CREATE INDEX idx_vendor_id ON vendor_configs(vendor_id);
CREATE INDEX idx_vendor_active ON vendor_configs(active);
```

## Testing

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests VendorConfigRepositoryTest

# Run with debug logging
./gradlew test --debug
```

### Test Database

- Tests use H2 in-memory database
- Schema automatically created from `src/test/resources/schema.sql`
- H2 configured in SQL Server compatibility mode
- Each test runs in isolation with fresh database

## XML Mappers (Optional)

While the current implementation uses annotation-based mappers, you can also use XML mappers for complex queries:

### Example: VendorConfigMapper.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.vendorauth.mapper.VendorConfigMapper">
    
    <resultMap id="vendorConfigResultMap" type="com.vendorauth.entity.VendorConfig">
        <id property="id" column="id"/>
        <result property="vendorId" column="vendor_id"/>
        <result property="vendorName" column="vendor_name"/>
        <result property="authType" column="auth_type"/>
        <result property="authDetailsJson" column="auth_details_json"/>
        <result property="active" column="active"/>
        <result property="baseUrl" column="base_url"/>
        <result property="timeoutSeconds" column="timeout_seconds"/>
        <result property="maxRetries" column="max_retries"/>
        <result property="description" column="description"/>
        <result property="createdAt" column="created_at"/>
        <result property="updatedAt" column="updated_at"/>
    </resultMap>
    
    <select id="findById" resultMap="vendorConfigResultMap">
        SELECT * FROM vendor_configs WHERE id = #{id}
    </select>
    
    <!-- Add more queries as needed -->
</mapper>
```

Place XML mappers in `src/main/resources/mapper/` directory.

## Best Practices

1. **Use Parameterized Queries**: Always use `#{param}` to prevent SQL injection
2. **Index Strategy**: Create indexes on frequently queried columns
3. **Connection Pooling**: HikariCP is configured by default
4. **Transaction Management**: Use `@Transactional` for multi-statement operations
5. **Logging**: Enable SQL logging in development for debugging
6. **Type Handlers**: Create custom type handlers for complex types
7. **Result Maps**: Use XML result maps for complex object mapping

## Troubleshooting

### Common Issues:

1. **Mapper Not Found**
   - Ensure `@MapperScan` is configured
   - Check mapper interface is in correct package
   - Verify mapper interface has `@Mapper` annotation

2. **Type Conversion Errors**
   - Create custom type handler for complex types
   - Register type handler in configuration

3. **SQL Syntax Errors**
   - Enable SQL logging to see actual queries
   - Test queries directly in database tool
   - Check SQL Server vs H2 syntax differences

4. **Test Failures**
   - Verify test schema matches production schema
   - Check H2 compatibility mode is set correctly
   - Ensure test data is properly set up

## Performance Tuning

1. **Batch Operations**: Use MyBatis batch executor for bulk inserts/updates
2. **Lazy Loading**: Configure lazy loading for large result sets
3. **Caching**: Enable MyBatis second-level cache if needed
4. **Connection Pool**: Tune HikariCP settings based on load

## Future Enhancements

1. **XML Mappers**: Move complex queries to XML for better maintainability
2. **Dynamic SQL**: Use MyBatis dynamic SQL for conditional queries
3. **Stored Procedures**: Add support for SQL Server stored procedures
4. **Pagination**: Implement PageHelper for paginated queries
5. **Audit Trail**: Add automatic audit fields (created_by, updated_by)

## Compatibility

- Java 17
- Spring Boot 3.2.0
- MyBatis Spring Boot Starter 3.0.3
- SQL Server 2016+
- H2 2.2.224 (for testing)
