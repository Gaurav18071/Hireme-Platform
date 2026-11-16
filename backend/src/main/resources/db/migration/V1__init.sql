-- V1: Initial schema - reusable updated_at trigger function
-- Follows CLAUDE.md conventions: UUID primary keys, TIMESTAMPTZ, set_updated_at()

-- Create a reusable trigger function to set updated_at on UPDATE
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Example usage (Phase 2+ will create real tables):
-- Every table with updated_at column can use this trigger:
-- CREATE TRIGGER trigger_set_updated_at
--     BEFORE UPDATE ON table_name
--     FOR EACH ROW
--     EXECUTE FUNCTION set_updated_at();
