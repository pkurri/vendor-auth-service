# Keystore Setup for JWT Signing

This directory should contain the keystore file used for JWT token signing.

## Generating a PKCS12 Keystore

### Using keytool (Java 17)

```bash
# Generate RSA key pair (recommended for production)
keytool -genkeypair -alias auth-key \
  -keyalg RSA -keysize 2048 \
  -storetype PKCS12 \
  -keystore auth-jwt.p12 \
  -storepass changeit \
  -keypass changeit \
  -validity 3650 \
  -dname "CN=Vendor Auth Service, OU=Engineering, O=YourCompany, L=City, ST=State, C=US"

# Generate EC key pair (alternative, more efficient)
keytool -genkeypair -alias auth-key \
  -keyalg EC -groupname secp256r1 \
  -storetype PKCS12 \
  -keystore auth-jwt.p12 \
  -storepass changeit \
  -keypass changeit \
  -validity 3650 \
  -dname "CN=Vendor Auth Service, OU=Engineering, O=YourCompany, L=City, ST=State, C=US"
```

### Verify the keystore

```bash
keytool -list -v -keystore auth-jwt.p12 -storetype PKCS12 -storepass changeit
```

## Configuration

Update `application.yml` with the keystore details:

```yaml
auth:
  signing:
    keystore: classpath:keystore/auth-jwt.p12
    store-password: changeit
    key-password: changeit
    type: PKCS12
    active-alias: auth-key
```

## Security Notes

1. **Never commit the actual keystore file to version control**
2. Use environment variables for passwords in production:
   - `KEYSTORE_PASSWORD`
   - `KEY_PASSWORD`
3. Store the keystore securely (e.g., AWS Secrets Manager, Azure Key Vault)
4. Rotate keys periodically using the `previous-alias` feature for zero-downtime rotation

## Key Rotation

To rotate keys without downtime:

1. Generate a new key with a different alias (e.g., `auth-key-2`)
2. Update `active-alias` to `auth-key-2`
3. Set `previous-alias` to `auth-key` (old key)
4. Both keys will be published in JWKS
5. After all tokens with old key expire, remove `previous-alias`
