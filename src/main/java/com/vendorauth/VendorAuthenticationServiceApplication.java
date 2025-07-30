package com.vendorauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Vendor Authentication Service.
 * 
 * This service provides a flexible framework for authenticating against
 * various external vendors with different authentication mechanisms.
 */
@SpringBootApplication
public class VendorAuthenticationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VendorAuthenticationServiceApplication.class, args);
    }
}
