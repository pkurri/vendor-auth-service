package com.vendorauth.security;

import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.util.ByteSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * ShiroPasswordEncoder implements Spring Security's PasswordEncoder
 * using Shiro's hashing capabilities for consistency with Shiro's security model.
 */
@Component
public class ShiroPasswordEncoder implements PasswordEncoder {
    
    private final DefaultHashService hashService;
    private final String algorithmName = "SHA-256";
    private final int hashIterations = 500000;
    private final boolean generatePublicSalt = true;
    
    public ShiroPasswordEncoder() {
        this.hashService = new DefaultHashService();
        this.hashService.setHashAlgorithmName(algorithmName);
        this.hashService.setHashIterations(hashIterations);
        this.hashService.setGeneratePublicSalt(generatePublicSalt);
    }
    
    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Raw password cannot be null");
        }
        
        HashRequest request = new HashRequest.Builder()
                .setSource(ByteSource.Util.bytes(rawPassword.toString()))
                .build();
                
        return hashService.computeHash(request).toHex();
    }
    
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || encodedPassword.trim().length() == 0) {
            return false;
        }
        
        String hashedPassword = encode(rawPassword);
        return encodedPassword.equals(hashedPassword);
    }
    
    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        // If you need to upgrade the encoding in the future, implement this
        return false;
    }
}
