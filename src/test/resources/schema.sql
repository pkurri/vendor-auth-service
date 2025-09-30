-- Test schema for H2 database (compatible with SQL Server syntax)
DROP TABLE IF EXISTS vendor_configs;

CREATE TABLE vendor_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_id VARCHAR(255) NOT NULL UNIQUE,
    vendor_name VARCHAR(255) NOT NULL,
    auth_type VARCHAR(50) NOT NULL,
    auth_details_json CLOB,
    active BIT NOT NULL DEFAULT 1,
    base_url VARCHAR(500),
    timeout_seconds INT DEFAULT 30,
    max_retries INT DEFAULT 3,
    description VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vendor_id ON vendor_configs(vendor_id);
CREATE INDEX idx_vendor_active ON vendor_configs(active);
