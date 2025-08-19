package com.vendorauth.security;

import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * ShiroPasswordEncoder implements Spring Security's PasswordEncoder
 * using Shiro's hashing capabilities for consistency with Shiro's security model.
 */
@Component
public class ShiroPasswordEncoder implements PasswordEncoder {
    
    // Iteration count for hashing
    private final int hashIterations = 500000;
    
    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Raw password cannot be null");
        }
        // No salt used here; consider adding and storing a per-user salt if needed
        return new Sha256Hash(rawPassword.toString(), null, hashIterations).toHex();
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
