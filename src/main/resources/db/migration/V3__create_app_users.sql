-- V3__create_app_users.sql
-- Place in: src/main/resources/db/migration/

CREATE TABLE app_users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(60) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(10) NOT NULL CHECK (role IN ('ADMIN','STAFF')),
    enabled       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
ALTER TABLE app_users ENABLE ROW LEVEL SECURITY;

-- Audit trail: which staff member added each donor
ALTER TABLE donors ADD COLUMN created_by VARCHAR(60);
