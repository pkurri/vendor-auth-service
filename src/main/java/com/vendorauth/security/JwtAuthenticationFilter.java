package com.vendorauth.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT Authentication Filter that processes JWT tokens in the Authorization header.
 * This filter validates JWT tokens and sets up the Spring Security context.
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String TOKEN_TYPE = "JWT";
    
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, 
                                 UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                if (tokenProvider.validateToken(jwt)) {
                    // Get user identity and set it on the spring security context
                    Authentication authentication = tokenProvider.getAuthentication(jwt);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT token: {}", ex.getMessage());
            setErrorResponse(response, "Expired JWT token", HttpStatus.UNAUTHORIZED);
            return;
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
            setErrorResponse(response, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
            return;
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty: {}", ex.getMessage());
            setErrorResponse(response, "JWT claims string is empty", HttpStatus.BAD_REQUEST);
            return;
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context: {}", ex.getMessage(), ex);
            setErrorResponse(response, "Authentication error", HttpStatus.INTERNAL_SERVER_ERROR);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from the Authorization header or from a request parameter.
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // 1. Try to get token from Authorization header
        String bearerToken = request.getHeader(HEADER_STRING);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        
        // 2. Try to get token from request parameter
        String token = request.getParameter("token");
        if (StringUtils.hasText(token)) {
            return token;
        }
        
        return null;
    }
    
    /**
     * Set error response for unauthorized requests
     */
    private void setErrorResponse(HttpServletResponse response, String message, HttpStatus status) 
            throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
            "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
            status.value(),
            status.getReasonPhrase(),
            message,
            "" // You can add the request path here if needed
        ));
    }
}
