# Quick Start Guide

## Prerequisites

- Java 17 (JDK 17)
- SQL Server 2016+ or compatible database
- Gradle (or use wrapper)

## Setup Steps

### 1. Database Setup

Run the schema creation script on your SQL Server:

```sql
-- Execute src/main/resources/schema.sql
```

### 2. Configure Database Connection

Update `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=vendorauth;encrypt=true;trustServerCertificate=true;
    username: sa
    password: your_strong_password
```

### 3. Generate Keystore (for OAuth2)

```bash
cd src/main/resources/keystore

# Generate RSA key pair
keytool -genkeypair -alias auth-key \
  -keyalg RSA -keysize 2048 \
  -storetype PKCS12 \
  -keystore auth-jwt.p12 \
  -storepass changeit \
  -keypass changeit \
  -validity 3650 \
  -dname "CN=Vendor Auth Service, OU=Engineering, O=YourCompany, L=City, ST=State, C=US"
```

### 4. Set Environment Variables (Optional)

```bash
# Windows PowerShell
$env:KEYSTORE_PASSWORD="your_secure_password"
$env:KEY_PASSWORD="your_secure_password"
$env:JWT_SECRET="your-512-bit-secret-key"

# Linux/Mac
export KEYSTORE_PASSWORD=your_secure_password
export KEY_PASSWORD=your_secure_password
export JWT_SECRET=your-512-bit-secret-key
```

### 5. Build the Project

```bash
# Using Gradle wrapper (recommended)
./gradlew build

# Or using installed Gradle
gradle build
```

### 6. Run Tests

```bash
./gradlew test
```

### 7. Start the Application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

## Verify Installation

### 1. Check Health Endpoint

```bash
curl http://localhost:8080/actuator/health
```

### 2. Check OpenID Configuration

```bash
curl http://localhost:8080/.well-known/openid-configuration
```

### 3. Request OAuth2 Token

```bash
curl -X POST http://localhost:8080/oauth2/token \
  -u m2m-client:secret \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&scope=read"
```

### 4. Check JWKS Endpoint

```bash
curl http://localhost:8080/oauth2/jwks
```

### 5. Access Swagger UI

Open browser: `http://localhost:8080/swagger-ui.html`

## MyBatis Usage

### Query Vendor Configurations

```java
@Autowired
private VendorConfigRepository repository;

// Find by vendor ID
Optional<VendorConfig> vendor = repository.findByVendorId("test-vendor");

// Find all active vendors
List<VendorConfig> activeVendors = repository.findByActiveTrue();

// Find by auth type
List<VendorConfig> oauth2Vendors = repository.findByAuthType(AuthType.OAUTH2);

// Save new vendor
VendorConfig newVendor = VendorConfig.builder()
    .vendorId("new-vendor")
    .vendorName("New Vendor")
    .authType(AuthType.API_KEY)
    .active(true)
    .build();
repository.save(newVendor);
```

### Custom Queries

Add to `VendorConfigMapper.java`:

```java
@Select("SELECT * FROM vendor_configs WHERE base_url LIKE #{pattern}")
List<VendorConfig> findByBaseUrlPattern(@Param("pattern") String pattern);
```

## OAuth2 Usage

### Client Credentials Flow

```bash
# Get access token
TOKEN=$(curl -s -X POST http://localhost:8080/oauth2/token \
  -u m2m-client:secret \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&scope=read" \
  | jq -r '.access_token')

# Use token to access protected endpoint
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/vendors
```

### Add New OAuth2 Client

Update `SecurityConfig.java`:

```java
@Bean
public RegisteredClientRepository registeredClientRepository(PasswordEncoder encoder) {
    RegisteredClient m2m = RegisteredClient.withId(UUID.randomUUID().toString())
        .clientId("m2m-client")
        .clientSecret(encoder.encode("secret"))
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
        .scope("read")
        .build();
    
    RegisteredClient newClient = RegisteredClient.withId(UUID.randomUUID().toString())
        .clientId("your-client-id")
        .clientSecret(encoder.encode("your-client-secret"))
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
        .scope("read")
        .scope("write")
        .build();

    return new InMemoryRegisteredClientRepository(m2m, newClient);
}
```

## Common Tasks

### Add New Vendor Configuration

```bash
curl -X POST http://localhost:8080/api/v1/vendors \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "vendorId": "new-vendor",
    "vendorName": "New Vendor",
    "authType": "OAUTH2",
    "authDetailsJson": "{\"clientId\":\"xxx\",\"clientSecret\":\"yyy\"}",
    "baseUrl": "https://api.newvendor.com",
    "active": true
  }'
```

### Authenticate with Vendor

```bash
curl -X POST http://localhost:8080/api/v1/authenticate/new-vendor \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test-user",
    "password": "test-password"
  }'
```

### Update Vendor Configuration

```bash
curl -X PUT http://localhost:8080/api/v1/vendors/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "vendorId": "updated-vendor",
    "vendorName": "Updated Vendor",
    "authType": "API_KEY",
    "active": false
  }'
```

## Troubleshooting

### Application Won't Start

1. Check database connection in `application.yml`
2. Verify SQL Server is running
3. Check if keystore file exists
4. Review application logs

### Tests Failing

1. Check H2 database configuration
2. Verify test schema is correct
3. Run tests with debug logging: `./gradlew test --debug`

### OAuth2 Token Issues

1. Verify keystore is generated correctly
2. Check client credentials
3. Review JWKS endpoint output
4. Enable security debug logging

### MyBatis Mapper Not Found

1. Verify `@MapperScan` annotation in main class
2. Check mapper interface has `@Mapper` annotation
3. Ensure mapper is in correct package

## Development Tips

### Enable SQL Logging

In `application.yml`:

```yaml
logging:
  level:
    com.vendorauth: DEBUG
    org.mybatis: DEBUG
```

### Hot Reload

Use Spring Boot DevTools:

```gradle
dependencies {
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
}
```

### Database Migrations

Consider using Flyway or Liquibase for production:

```gradle
implementation 'org.flywaydb:flyway-core'
implementation 'org.flywaydb:flyway-sqlserver'
```

## Production Checklist

- [ ] Change default passwords
- [ ] Use environment variables for secrets
- [ ] Enable HTTPS
- [ ] Configure proper CORS
- [ ] Set up monitoring and logging
- [ ] Configure connection pool settings
- [ ] Enable security headers
- [ ] Set up database backups
- [ ] Configure rate limiting
- [ ] Review and update security policies

## Documentation

- **OAuth2 Setup**: See `OAUTH2_SETUP.md`
- **MyBatis Migration**: See `MYBATIS_MIGRATION.md`
- **Full Migration Summary**: See `MIGRATION_SUMMARY.md`
- **API Documentation**: Access Swagger UI at `/swagger-ui.html`

## Support

For issues or questions, refer to:
- Project documentation files
- MyBatis documentation: https://mybatis.org/mybatis-3/
- Spring Security OAuth2: https://spring.io/projects/spring-authorization-server
- Spring Boot documentation: https://spring.io/projects/spring-boot
