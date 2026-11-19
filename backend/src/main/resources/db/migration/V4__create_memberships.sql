-- V4: Memberships table (user-organization many-to-many with role)
-- A user can belong to multiple organizations with different roles in each

CREATE TABLE memberships (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    role            VARCHAR(50) NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Business constraints: only valid roles and statuses
    CONSTRAINT chk_memberships_role CHECK (
        role IN ('ADMIN', 'RECRUITER', 'HIRING_MANAGER', 'INTERVIEWER')
    ),
    CONSTRAINT chk_memberships_status CHECK (
        status IN ('ACTIVE', 'INACTIVE')
    )
);

-- Unique constraint: one user cannot have duplicate memberships in the same organization
CREATE UNIQUE INDEX idx_memberships_user_org ON memberships (user_id, organization_id);

-- Query optimization: find all members of an organization
CREATE INDEX idx_memberships_organization_id ON memberships (organization_id);

-- Query optimization: find all organizations a user belongs to
CREATE INDEX idx_memberships_user_id ON memberships (user_id);

CREATE TRIGGER trigger_set_updated_at_memberships
    BEFORE UPDATE ON memberships
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

COMMENT ON TABLE memberships IS 'User roles within organizations (tenant membership)';
COMMENT ON COLUMN memberships.role IS 'ADMIN | RECRUITER | HIRING_MANAGER | INTERVIEWER';
COMMENT ON COLUMN memberships.status IS 'ACTIVE | INACTIVE (for soft suspension)';
COMMENT ON COLUMN memberships.user_id IS 'Foreign key to users table';
COMMENT ON COLUMN memberships.organization_id IS 'Foreign key to organizations table';
