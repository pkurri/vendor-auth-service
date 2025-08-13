-- Create the vendor_configs table if it doesn't exist
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'vendor_configs')
BEGIN
    CREATE TABLE vendor_configs (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        vendor_id NVARCHAR(255) NOT NULL,
        vendor_name NVARCHAR(255) NOT NULL,
        auth_type NVARCHAR(50) NOT NULL,
        auth_details_json NVARCHAR(MAX),
        active BIT NOT NULL DEFAULT 1,
        base_url NVARCHAR(500),
        timeout_seconds INT NOT NULL DEFAULT 30,
        max_retries INT NOT NULL DEFAULT 3,
        description NVARCHAR(1000),
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT uk_vendor_id UNIQUE (vendor_id)
    );

    -- Create indexes
    CREATE INDEX idx_vendor_id ON vendor_configs(vendor_id);
    CREATE INDEX idx_vendor_active ON vendor_configs(active);
    
    PRINT 'Created vendor_configs table and indexes';
END
ELSE
BEGIN
    PRINT 'vendor_configs table already exists';
END

-- Add any missing columns that might be needed
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('vendor_configs') AND name = 'auth_details_json')
BEGIN
    ALTER TABLE vendor_configs ADD auth_details_json NVARCHAR(MAX);
    PRINT 'Added auth_details_json column';
END
