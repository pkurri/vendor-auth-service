# Vendor Authentication Service

A flexible Spring Boot service for authenticating against external vendors with diverse authentication mechanisms.

## Overview

This service provides a robust and adaptable framework for integrating with external vendors that have different authentication requirements (OAuth2, API keys, custom flows, etc.). The system prioritizes security, scalability, and ease of integration without requiring prior knowledge of vendor-specific authentication details.

## Architecture

### Core Components

- **VendorAuthenticator Interface**: Abstraction for different authentication mechanisms
- **VendorConfig Entity**: Flexible storage for vendor-specific authentication details
- **VendorAuthenticationService**: Coordinator that selects appropriate authenticators
- **AuthType Enum**: Supported authentication types (NOOP, OAUTH2, API_KEY, BASIC_AUTH, JWT_TOKEN, CUSTOM)

### Database Schema

```sql
CREATE TABLE vendor_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vendor_id VARCHAR(255) UNIQUE NOT NULL,
    vendor_name VARCHAR(255) NOT NULL,
    auth_type VARCHAR(50) NOT NULL,
    auth_details_json TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    base_url VARCHAR(500),
    timeout_seconds INTEGER DEFAULT 30,
    max_retries INTEGER DEFAULT 3,
    description VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

## Getting Started

### Prerequisites

- Java 17+
- Gradle 8.5+

### Dependencies

- Spring Boot 3.2.0
- Spring Web, Security, Data JPA
- Apache Shiro 1.13.0 (alternative security)
- H2 Database (development)
- Lombok
- Jackson (JSON processing)

### Running the Application

```bash
# Clone the repository
git clone <repository-url>
cd vendor-authentication-service

# Run with Spring Security (default)
./gradlew bootRun

# Run with Apache Shiro
./gradlew bootRun --args='--spring.profiles.active=shiro'
```

The application will start on `http://localhost:8080`

### H2 Console

Access the H2 database console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:vendorauth`
- Username: `sa`
- Password: (empty)

## API Endpoints

### Authentication Endpoint

```http
POST /api/v1/authenticate/vendor/{vendorId}
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "password123",
  "token": "api-key-or-jwt-token",
  "clientId": "oauth2-client-id",
  "clientSecret": "oauth2-client-secret",
  "additionalParams": {
    "custom_field": "custom_value"
  }
}
```

### Health Check

```http
GET /api/v1/authenticate/health
```

### Vendor Status

```http
GET /api/v1/authenticate/vendor/{vendorId}/status
```

## Sample Vendor Configurations

The application initializes with sample vendor configurations:

### 1. NoOp Vendor (Active)
```json
{
  "vendorId": "test-vendor",
  "authType": "NOOP",
  "authDetailsJson": "{\"description\": \"No-operation test vendor\"}"
}
```

### 2. API Key Vendor (Inactive - for reference)
```json
{
  "vendorId": "apikey-vendor",
  "authType": "API_KEY",
  "authDetailsJson": {
    "apiKeyHeader": "X-API-Key",
    "apiKeyQueryParam": "api_key",
    "authMethod": "header"
  }
}
```

### 3. OAuth2 Vendor (Inactive - for reference)
```json
{
  "vendorId": "oauth2-vendor",
  "authType": "OAUTH2",
  "authDetailsJson": {
    "clientId": "sample_client_id",
    "clientSecret": "sample_client_secret",
    "authUrl": "https://api.oauth2vendor.com/oauth/authorize",
    "tokenUrl": "https://api.oauth2vendor.com/oauth/token",
    "scope": "read write"
  }
}
```

## Testing

### Test NoOp Authentication

```bash
curl -X POST http://localhost:8080/api/v1/authenticate/vendor/test-vendor \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "testpass"}'
```

### Test API Key Authentication

```bash
curl -X POST http://localhost:8080/api/v1/authenticate/vendor/apikey-vendor \
  -H "Content-Type: application/json" \
  -d '{"token": "valid-api-key-12345"}'
```

## Extending the Service

### Adding New Authentication Types

1. **Create Authenticator Implementation**:
   ```java
   @Component
   public class CustomAuthenticator implements VendorAuthenticator {
       // Implement required methods
   }
   ```

2. **Register in AuthenticatorConfig**:
   ```java
   authenticators.put(AuthType.CUSTOM, applicationContext.getBean(CustomAuthenticator.class));
   ```

3. **Add to AuthType Enum** (if new type):
   ```java
   public enum AuthType {
       // ... existing types
       NEW_AUTH_TYPE
   }
   ```

### Configuration Examples

#### OAuth2 Configuration
```json
{
  "clientId": "your_client_id",
  "clientSecret": "your_client_secret",
  "authUrl": "https://vendor.com/oauth/authorize",
  "tokenUrl": "https://vendor.com/oauth/token",
  "scope": "read write",
  "redirectUri": "https://yourapp.com/callback"
}
```

#### API Key Configuration
```json
{
  "apiKeyHeader": "X-API-Key",
  "apiKeyQueryParam": "api_key",
  "authMethod": "header"
}
```

#### Basic Auth Configuration
```json
{
  "realm": "VendorAPI",
  "encoding": "UTF-8"
}
```

## Security Considerations

### Development vs Production

**Current (Development)**:
- All authentication endpoints are open
- H2 console is accessible
- No API key validation for incoming requests

**Production TODO**:
- Implement API key validation for incoming requests
- Add rate limiting and request throttling
- Configure IP whitelisting for trusted clients
- Implement request signing/verification
- Use production database (PostgreSQL, MySQL)
- Enable HTTPS only
- Add comprehensive logging and monitoring

### Security Configuration Options

The service supports both Spring Security and Apache Shiro:

- **Spring Security**: Default configuration (recommended)
- **Apache Shiro**: Alternative option (use `--spring.profiles.active=shiro`)

## Monitoring and Logging

- Application logs vendor authentication attempts
- Debug logging available for troubleshooting
- Health check endpoint for monitoring
- Vendor status endpoint for configuration validation

## Next Steps

1. **Implement Concrete Authenticators**:
   - OAuth2Authenticator
   - BasicAuthAuthenticator
   - JwtTokenAuthenticator

2. **Production Security**:
   - API key validation
   - Rate limiting
   - Request signing

3. **Enhanced Features**:
   - Vendor configuration management API
   - Authentication result caching
   - Retry mechanisms with exponential backoff
   - Comprehensive metrics and monitoring

## Contributing

1. Follow the existing code structure and patterns
2. Add comprehensive tests for new authenticators
3. Update documentation for new authentication types
4. Ensure security best practices are followed
