package com.vendorauth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for JWT authentication request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthenticationRequest {
    @NotBlank(message = "Username cannot be blank")
    private String username;
    
    @NotBlank(message = "Password cannot be blank")
    private String password;
}
