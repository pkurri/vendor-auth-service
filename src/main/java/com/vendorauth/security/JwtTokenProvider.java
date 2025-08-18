package com.vendorauth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JWT Token provider for generating and validating tokens
 */
@Component
@Slf4j
public class JwtTokenProvider {

    // JWT configuration with defaults
    @Value("${app.jwt.secret:defaultSecretKeyForJwtTokenGenerationWithMinimum32Chars}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400000}") // 24 hours
    private long jwtExpirationInMs;

    @Value("${app.jwt.refreshExpiration:604800000}") // 7 days
    private long refreshExpirationInMs;

    @Value("${app.jwt.issuer:vendor-auth-service}")
    private String jwtIssuer;

    /**
     * Generate a JWT token for the specified subject with custom claims
     * @param subject The subject (typically user ID or username)
     * @param claims Additional claims to include in the token
     * @param expirationInMs Token expiration time in milliseconds
     * @return JWT token as a string
     */
    public String generateToken(String subject, Map<String, Object> claims, long expirationInMs) {
        return Jwts.builder()
                .setClaims(claims != null ? claims : new HashMap<>())
                .setSubject(subject)
                .setIssuer(jwtIssuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationInMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Generate a standard access token with default expiration
     */
    public String generateAccessToken(String subject, Map<String, Object> claims) {
        return generateToken(subject, claims, jwtExpirationInMs);
    }

    /**
     * Generate a refresh token with longer expiration
     */
    public String generateRefreshToken(String subject) {
        return generateToken(subject, null, refreshExpirationInMs);
    }

    /**
     * Generate a token with custom expiration
     */
    public String generateToken(String subject, Map<String, Object> claims, Date expiration) {
        return Jwts.builder()
                .setClaims(claims != null ? claims : new HashMap<>())
                .setSubject(subject)
                .setIssuer(jwtIssuer)
                .setIssuedAt(new Date())
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Generate a token for a UserDetails object
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Add user roles/authorities to claims
        if (userDetails.getAuthorities() != null) {
            claims.put("roles", userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
        }
        return generateAccessToken(userDetails.getUsername(), claims);
    }

    /**
     * Convenience method to generate an access token for a plain subject (e.g., username).
     */
    public String generateToken(String subject) {
        return generateAccessToken(subject, null);
    }

    /**
     * Validate the JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }

    /**
     * Get username from JWT token
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Get expiration date from JWT token
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Get a specific claim from JWT token
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Get all claims from JWT token
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Check if the token has expired
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Create Authentication object from JWT token
     */
    public Authentication getAuthentication(String token) {
        Claims claims = getAllClaimsFromToken(token);
        String username = claims.getSubject();
        
        // Extract roles from claims
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles") != null ? 
                (List<String>) claims.get("roles") : Collections.emptyList();
        
        // Convert roles to GrantedAuthority objects
        Collection<? extends GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());

        // Create UserDetails object
        UserDetails userDetails = User.builder()
                .username(username)
                .password("")
                .authorities(authorities)
                .build();

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    /**
     * Get the signing key for JWT
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate token from Authentication object
     */
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return generateToken(userPrincipal);
    }

    /**
     * Generate refresh token from Authentication object
     */
    public String generateRefreshToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return generateRefreshToken(userPrincipal.getUsername());
    }
}
