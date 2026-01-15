-- ============================================================
-- FinShark Seed: 001_common.sql
-- Description: Common functions and utilities used by all modules
-- Idempotent: Yes (CREATE OR REPLACE)
-- ============================================================

-- ------------------------------------------------------------
-- 1. update_updated_at_column() - Auto-update timestamp trigger function
-- ------------------------------------------------------------
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION update_updated_at_column() IS 'Trigger function to auto-update updated_at column';

-- ============================================================
-- End of 001_common.sql
-- ============================================================
