-- V6: Notes table (test resource for tenant isolation)
-- This table exists ONLY to test tenant isolation in Phase 2.
-- It will be removed or replaced once Jobs are implemented in Phase 4.

CREATE TABLE notes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    title           VARCHAR(255) NOT NULL,
    content         TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- CRITICAL INDEX: All queries filter by organization_id for tenant isolation
CREATE INDEX idx_notes_organization_id ON notes (organization_id);

CREATE TRIGGER trigger_set_updated_at_notes
    BEFORE UPDATE ON notes
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

COMMENT ON TABLE notes IS 'Test resource for tenant isolation (removed in Phase 4)';
COMMENT ON COLUMN notes.organization_id IS 'Tenant isolation: every note belongs to one organization';
