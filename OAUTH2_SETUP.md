# OAuth2 Authorization Server Setup

## Overview

This document describes the OAuth2 Authorization Server configuration added to the Vendor Authentication Service.

## Changes Made

### 1. Configuration Files

#### `AuthSigningProperties.java`
- Configuration properties for keystore-based JWT signing
- Supports PKCS12 and JKS keystore types
- Includes validation annotations for required fields
- Supports key rotation via `activeAlias` and `previousAlias`

#### `KeystoreJwksConfig.java`
- Loads keystore and extracts public/private key pairs
- Builds JWK (JSON Web Key) sets for RSA and EC keys
- Publishes JWKS endpoint for token validation
- Supports multiple keys for rotation scenarios

#### `SecurityConfig.java`
- OAuth2 Authorization Server configuration
- Two security filter chains:
  1. Authorization Server endpoints (Order 1)
  2. Default security for other endpoints (Order 2)
- In-memory client registration (M2M client)
- Form-based login for authorization code flow

### 2. Dependencies Added

```gradle
// Spring OAuth2 Authorization Server
implementation 'org.springframework.boot:spring-boot-starter-oauth2-authorization-server'

// Nimbus JOSE JWT for JWKS support
implementation 'com.nimbusds:nimbus-jose-jwt:9.37.3'
```

### 3. Application Configuration

Added to `application.yml`:

```yaml
auth:
  signing:
    keystore: classpath:keystore/auth-jwt.p12
    store-password: ${KEYSTORE_PASSWORD:changeit}
    key-password: ${KEY_PASSWORD:changeit}
    type: PKCS12
    active-alias: auth-key
    previous-alias: # Optional: for key rotation
    kid: # Optional: override key ID
```

### 4. Keystore Setup

Created `src/main/resources/keystore/` directory with:
- `README.md` - Instructions for generating keystores
- `.gitignore` - Prevents committing sensitive keystore files

## OAuth2 Endpoints

Once configured, the following endpoints are available:

- **Authorization Endpoint**: `GET /oauth2/authorize`
- **Token Endpoint**: `POST /oauth2/token`
- **JWKS Endpoint**: `GET /oauth2/jwks`
- **Token Introspection**: `POST /oauth2/introspect`
- **Token Revocation**: `POST /oauth2/revoke`
- **OpenID Configuration**: `GET /.well-known/openid-configuration`

## Client Credentials Flow (M2M)

The default configuration includes an M2M client:

```bash
# Request token
curl -X POST http://localhost:8080/oauth2/token \
  -u m2m-client:secret \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&scope=read"
```

## Next Steps

1. **Generate Keystore**: Follow instructions in `src/main/resources/keystore/README.md`
2. **Configure Environment Variables**: Set `KEYSTORE_PASSWORD` and `KEY_PASSWORD`
3. **Database-backed Clients**: Replace `InMemoryRegisteredClientRepository` with JPA-based repository
4. **User Authentication**: Implement `UserDetailsService` for authorization code flow
5. **Customize Scopes**: Define application-specific scopes and permissions

## Security Considerations

1. Never commit keystore files to version control
2. Use strong passwords for keystore and keys
3. Rotate keys periodically using the `previous-alias` feature
4. Store keystores securely in production (e.g., AWS Secrets Manager)
5. Use HTTPS in production
6. Implement proper client authentication for production clients

## Testing

To test the OAuth2 setup:

1. Start the application
2. Access the OpenID configuration: `http://localhost:8080/.well-known/openid-configuration`
3. Request a token using client credentials (see example above)
4. Verify the JWKS endpoint: `http://localhost:8080/oauth2/jwks`

## Compatibility

- Java 17
- Spring Boot 3.2.0
- Spring Security 6.x
- OAuth2 Authorization Server 1.x
