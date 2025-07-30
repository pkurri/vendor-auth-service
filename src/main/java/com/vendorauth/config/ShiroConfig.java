package com.vendorauth.config;

import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Apache Shiro configuration for the Vendor Authentication API Service.
 * 
 * This configuration is activated with the 'shiro' profile.
 * Use this as an alternative to Spring Security for API security.
 * 
 * To use Shiro instead of Spring Security:
 * 1. Set spring.profiles.active=shiro in application.yml
 * 2. Exclude Spring Security auto-configuration if needed
 * 
 * DEVELOPMENT: Permits all requests to authentication endpoints.
 * PRODUCTION TODO: Implement proper API security with Shiro realms.
 */
@Configuration
@Profile("shiro")
public class ShiroConfig {
    
    /**
     * Shiro Security Manager configuration
     */
    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(realm());
        return securityManager;
    }
    
    /**
     * Basic realm configuration for development.
     * In production, replace with custom realm for API key validation.
     */
    @Bean
    public Realm realm() {
        // Simple INI realm for development
        IniRealm realm = new IniRealm("classpath:shiro.ini");
        return realm;
    }
    
    /**
     * Shiro Filter Factory Bean - defines URL patterns and access rules
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        
        // Define filter chain definitions for API endpoints
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        
        // Allow all requests to authentication endpoints (DEVELOPMENT ONLY)
        filterChainDefinitionMap.put("/api/v1/authenticate/**", "anon");
        
        // Allow H2 console access (DEVELOPMENT ONLY)
        filterChainDefinitionMap.put("/h2-console/**", "anon");
        
        // Allow health and status endpoints
        filterChainDefinitionMap.put("/actuator/**", "anon");
        
        // For API-only service, permit all other requests for now
        filterChainDefinitionMap.put("/**", "anon");
        
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        
        return shiroFilterFactoryBean;
    }
}
