-- V2: Users table for authentication
-- Email is globally unique (case-insensitive), users can belong to multiple organizations

CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Case-insensitive unique email: prevents user@example.com and User@Example.com
CREATE UNIQUE INDEX idx_users_email_lower ON users (LOWER(email));

-- Trigger for updated_at (uses function from V1)
CREATE TRIGGER trigger_set_updated_at_users
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

COMMENT ON TABLE users IS 'User accounts for authentication (global, not tenant-scoped)';
COMMENT ON COLUMN users.email IS 'Unique email, normalized to lowercase on insert';
COMMENT ON COLUMN users.password_hash IS 'BCrypt hash (work factor 12)';
