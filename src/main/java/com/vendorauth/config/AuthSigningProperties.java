package com.vendorauth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "auth.signing")
public class AuthSigningProperties {

    /** Keystore location, e.g. classpath:keystore/auth-jwt.p12 or file:/etc/keys/auth.p12 */
    @NotBlank
    private String keystore;
    
    /** Keystore password */
    @NotBlank
    private String storePassword;

    /** Private key entry password (often same as store password) */
    @NotBlank
    private String keyPassword;

    /** Keystore type: JKS or PKCS12 */
    @NotBlank
    @Pattern(regexp = "JKS|PKCS12", message = "type must be JKS or PKCS12")
    private String type = "PKCS12";

    /** Alias of the active key */
    @NotBlank
    private String activeAlias;

    /** Optional: previous alias to publish during rotation */
    private String previousAlias;

    /** Optional: override kid if blank it's derived from cert */
    private String kid;
}
