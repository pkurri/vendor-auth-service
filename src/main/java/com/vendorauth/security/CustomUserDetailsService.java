package com.vendorauth.security;

import com.vendorauth.entity.VendorConfig;
import com.vendorauth.repository.VendorConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom UserDetailsService implementation for JWT authentication
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final VendorConfigRepository vendorConfigRepository;

    @Autowired
    public CustomUserDetailsService(VendorConfigRepository vendorConfigRepository) {
        this.vendorConfigRepository = vendorConfigRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String vendorId) throws UsernameNotFoundException {
        // In a real application, you would load the user from the database
        // For this example, we'll use a simple in-memory user
        // You should replace this with your actual user loading logic
        
        // Check if vendor exists and is active using available repository methods
        VendorConfig vendorConfig = vendorConfigRepository.findByVendorId(vendorId)
                .filter(cfg -> Boolean.TRUE.equals(cfg.getActive()))
                .orElseThrow(() ->
                    new UsernameNotFoundException("Vendor not found or inactive with id: " + vendorId)
                );
        
        // In a real application, you would get the password from the vendor config
        // For now, we'll use a placeholder password
        // The actual authentication will be handled by the appropriate VendorAuthenticator
        return new User(
                vendorConfig.getVendorId(),
                "{noop}password", // {noop} indicates NoOpPasswordEncoder
                Collections.emptyList()
        );
    }
}
