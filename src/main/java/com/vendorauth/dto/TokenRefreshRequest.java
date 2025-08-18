package com.vendorauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for token refresh request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRefreshRequest {
    @NotBlank(message = "Refresh token cannot be blank")
    private String refreshToken;
}
