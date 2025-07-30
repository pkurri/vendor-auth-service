package com.vendorauth.exception;

/**
 * Custom exception for authentication-related errors.
 * This exception is thrown when technical issues occur during authentication,
 * as opposed to invalid credentials which should be handled gracefully.
 */
public class AuthenticationException extends RuntimeException {
    
    private final String vendorId;
    private final String errorCode;
    
    public AuthenticationException(String message) {
        super(message);
        this.vendorId = null;
        this.errorCode = null;
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.vendorId = null;
        this.errorCode = null;
    }
    
    public AuthenticationException(String vendorId, String errorCode, String message) {
        super(message);
        this.vendorId = vendorId;
        this.errorCode = errorCode;
    }
    
    public AuthenticationException(String vendorId, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.vendorId = vendorId;
        this.errorCode = errorCode;
    }
    
    public String getVendorId() {
        return vendorId;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
