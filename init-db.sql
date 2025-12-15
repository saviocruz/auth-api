-- ============================================================================
-- PostgreSQL Initialization Script
-- ============================================================================

-- Create SCHEMA auth if not exists
CREATE SCHEMA IF NOT EXISTS auth;

-- Grant privileges to public
GRANT ALL PRIVILEGES ON SCHEMA auth TO postgres;

-- Ensure auth schema is in search_path
ALTER ROLE postgres SET search_path TO auth, public;
