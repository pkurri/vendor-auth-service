package com.vendorauth.config;

import com.vendorauth.mapper.VendorConfigMapper;
import com.vendorauth.repository.VendorConfigRepository;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Test configuration for MyBatis tests.
 * Provides necessary beans for repository testing.
 */
@TestConfiguration
public class TestMyBatisConfig {

    @Bean
    public VendorConfigRepository vendorConfigRepository(VendorConfigMapper mapper) {
        return new VendorConfigRepository(mapper);
    }
}
