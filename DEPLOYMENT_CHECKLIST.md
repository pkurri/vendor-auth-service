# Deployment Checklist

## Pre-Deployment Checklist

### 1. Database Setup
- [ ] SQL Server instance is running
- [ ] Database `vendorauth` is created
- [ ] Execute `schema.sql` to create tables
- [ ] Verify indexes are created
- [ ] Test database connection from application server
- [ ] Database user has appropriate permissions (SELECT, INSERT, UPDATE, DELETE)

### 2. Keystore Generation
- [ ] Navigate to `src/main/resources/keystore/`
- [ ] Generate keystore using keytool command
- [ ] Verify keystore file exists (`auth-jwt.p12`)
- [ ] Test keystore password
- [ ] Backup keystore securely
- [ ] Document keystore location and credentials

### 3. Environment Variables
- [ ] Set `KEYSTORE_PASSWORD` environment variable
- [ ] Set `KEY_PASSWORD` environment variable
- [ ] Set `JWT_SECRET` environment variable (if using JWT)
- [ ] Set database credentials (if not in config)
- [ ] Verify all environment variables are loaded

### 4. Configuration Files
- [ ] Update `application.yml` with production database URL
- [ ] Update database username/password
- [ ] Configure connection pool settings
- [ ] Set appropriate logging levels
- [ ] Configure CORS settings if needed
- [ ] Update OAuth2 issuer URL to production domain

### 5. Security Review
- [ ] Change default passwords
- [ ] Review OAuth2 client credentials
- [ ] Ensure HTTPS is enabled
- [ ] Configure security headers
- [ ] Review CORS policy
- [ ] Enable rate limiting if needed
- [ ] Review authentication endpoints

### 6. Build and Test
- [ ] Run `./gradlew clean build`
- [ ] All tests pass
- [ ] No compilation errors
- [ ] Review build warnings
- [ ] Test JAR file is created
- [ ] Verify JAR size is reasonable

### 7. Local Testing
- [ ] Start application locally
- [ ] Test health endpoint
- [ ] Test OAuth2 token endpoint
- [ ] Test JWKS endpoint
- [ ] Test vendor authentication
- [ ] Test database connectivity
- [ ] Review application logs

---

## Deployment Steps

### Step 1: Prepare Deployment Package
```bash
# Build the application
./gradlew clean build

# Package location
build/libs/vendor-auth-service-0.0.1-SNAPSHOT.jar
```

### Step 2: Transfer Files
- [ ] Copy JAR file to server
- [ ] Copy keystore file to secure location
- [ ] Copy configuration files
- [ ] Set appropriate file permissions

### Step 3: Server Setup
- [ ] Java 17 is installed
- [ ] Verify Java version: `java -version`
- [ ] Create application user (non-root)
- [ ] Create application directory
- [ ] Set up log directory
- [ ] Configure firewall rules

### Step 4: Environment Configuration
```bash
# Set environment variables
export KEYSTORE_PASSWORD=your_secure_password
export KEY_PASSWORD=your_secure_password
export JWT_SECRET=your_jwt_secret
export SPRING_PROFILES_ACTIVE=prod
```

### Step 5: Database Migration
- [ ] Backup existing database
- [ ] Run migration scripts
- [ ] Verify schema version
- [ ] Test database connectivity
- [ ] Seed initial data if needed

### Step 6: Start Application
```bash
# Start application
java -jar vendor-auth-service-0.0.1-SNAPSHOT.jar

# Or with custom configuration
java -jar vendor-auth-service-0.0.1-SNAPSHOT.jar \
  --spring.config.location=/path/to/application.yml
```

### Step 7: Verify Deployment
- [ ] Application starts without errors
- [ ] Health endpoint responds: `/actuator/health`
- [ ] OAuth2 endpoints accessible
- [ ] JWKS endpoint returns keys
- [ ] Database queries work
- [ ] Logs are being written

---

## Post-Deployment Verification

### 1. Health Checks
```bash
# Health endpoint
curl http://localhost:8080/actuator/health

# Expected: {"status":"UP"}
```

### 2. OAuth2 Endpoints
```bash
# OpenID Configuration
curl http://localhost:8080/.well-known/openid-configuration

# JWKS Endpoint
curl http://localhost:8080/oauth2/jwks

# Token Endpoint
curl -X POST http://localhost:8080/oauth2/token \
  -u m2m-client:secret \
  -d "grant_type=client_credentials&scope=read"
```

### 3. Database Connectivity
```bash
# Test vendor query
curl http://localhost:8080/api/v1/vendors \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Logging
- [ ] Application logs are being written
- [ ] Log rotation is configured
- [ ] Error logs are monitored
- [ ] SQL queries logged (if debug enabled)

### 5. Performance
- [ ] Response times are acceptable
- [ ] Database connection pool is healthy
- [ ] Memory usage is normal
- [ ] CPU usage is normal

---

## Monitoring Setup

### 1. Application Monitoring
- [ ] Set up health check monitoring
- [ ] Configure uptime monitoring
- [ ] Set up error alerting
- [ ] Monitor response times
- [ ] Track API usage

### 2. Database Monitoring
- [ ] Monitor connection pool
- [ ] Track query performance
- [ ] Monitor disk space
- [ ] Set up backup alerts
- [ ] Track database size

### 3. Security Monitoring
- [ ] Monitor failed authentication attempts
- [ ] Track token usage
- [ ] Monitor for suspicious activity
- [ ] Set up security alerts
- [ ] Review access logs

### 4. Log Aggregation
- [ ] Configure log shipping
- [ ] Set up log aggregation (ELK, Splunk, etc.)
- [ ] Create log dashboards
- [ ] Set up log alerts
- [ ] Configure log retention

---

## Production Configuration

### application-prod.yml
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://prod-db-server:1433;databaseName=vendorauth
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

mybatis:
  configuration:
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl

auth:
  signing:
    keystore: file:/etc/vendor-auth/keystore/auth-jwt.p12
    store-password: ${KEYSTORE_PASSWORD}
    key-password: ${KEY_PASSWORD}

logging:
  level:
    com.vendorauth: INFO
    org.springframework.security: WARN
    org.mybatis: WARN
  file:
    name: /var/log/vendor-auth-service/application.log
    max-size: 10MB
    max-history: 30

server:
  port: 8080
  ssl:
    enabled: true
    key-store: /etc/vendor-auth/ssl/keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

---

## Rollback Plan

### If Deployment Fails

1. **Stop Application**
   ```bash
   # Stop the service
   systemctl stop vendor-auth-service
   ```

2. **Restore Previous Version**
   ```bash
   # Restore previous JAR
   cp vendor-auth-service-previous.jar vendor-auth-service.jar
   ```

3. **Restore Database**
   ```bash
   # Restore database backup if schema changed
   sqlcmd -S server -d vendorauth -i backup.sql
   ```

4. **Restart Application**
   ```bash
   # Start the service
   systemctl start vendor-auth-service
   ```

5. **Verify Rollback**
   - [ ] Application starts successfully
   - [ ] Health checks pass
   - [ ] All endpoints functional
   - [ ] Database queries work

---

## Troubleshooting

### Application Won't Start

**Check:**
1. Java version: `java -version`
2. JAR file integrity
3. Configuration file syntax
4. Environment variables
5. Database connectivity
6. Port availability
7. File permissions

**Logs to Review:**
- Application startup logs
- Database connection logs
- Security configuration logs

### Database Connection Issues

**Check:**
1. Database server is running
2. Network connectivity
3. Firewall rules
4. Database credentials
5. Connection pool settings
6. Database permissions

**Test Connection:**
```bash
sqlcmd -S server -U username -P password -d vendorauth
```

### OAuth2 Token Issues

**Check:**
1. Keystore file exists and is readable
2. Keystore password is correct
3. Client credentials are correct
4. JWKS endpoint is accessible
5. Token expiration settings

**Debug:**
```bash
# Enable debug logging
export LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=DEBUG
```

### Performance Issues

**Check:**
1. Database query performance
2. Connection pool size
3. Memory settings
4. CPU usage
5. Network latency

**Optimize:**
- Increase connection pool size
- Add database indexes
- Enable caching
- Tune JVM settings

---

## Maintenance Tasks

### Daily
- [ ] Review error logs
- [ ] Check application health
- [ ] Monitor disk space
- [ ] Review security logs

### Weekly
- [ ] Review performance metrics
- [ ] Check database size
- [ ] Review backup status
- [ ] Update monitoring dashboards

### Monthly
- [ ] Review and rotate logs
- [ ] Update dependencies
- [ ] Security audit
- [ ] Performance tuning
- [ ] Backup testing

### Quarterly
- [ ] Key rotation
- [ ] Security review
- [ ] Disaster recovery test
- [ ] Capacity planning

---

## Emergency Contacts

- **Database Admin**: [Contact Info]
- **Security Team**: [Contact Info]
- **DevOps Team**: [Contact Info]
- **On-Call Engineer**: [Contact Info]

---

## Documentation Links

- **Setup Guide**: QUICK_START.md
- **OAuth2 Documentation**: OAUTH2_SETUP.md
- **MyBatis Guide**: MYBATIS_MIGRATION.md
- **Migration Summary**: MIGRATION_SUMMARY.md
- **Changes Log**: CHANGES.md

---

## Sign-Off

- [ ] Development Team Lead
- [ ] QA Team Lead
- [ ] Security Team
- [ ] Operations Team
- [ ] Product Owner

**Deployment Date**: _______________
**Deployed By**: _______________
**Approved By**: _______________

---

## Notes

_Add any deployment-specific notes here_
