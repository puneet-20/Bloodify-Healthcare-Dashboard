-- V1__create_donors.sql
-- Place in: src/main/resources/db/migration/

CREATE TABLE donors (
    id                    BIGSERIAL PRIMARY KEY,
    full_name             VARCHAR(120) NOT NULL,
    date_of_birth         DATE NOT NULL,
    gender                VARCHAR(10),
    blood_group           VARCHAR(3) NOT NULL
                          CHECK (blood_group IN ('A+','A-','B+','B-','AB+','AB-','O+','O-')),
    phone                 VARCHAR(15) NOT NULL,
    email                 VARCHAR(150) UNIQUE,
    city                  VARCHAR(80) NOT NULL,
    latitude              NUMERIC(9,6),
    longitude             NUMERIC(9,6),
    weight_kg             NUMERIC(5,2) CHECK (weight_kg >= 45),
    has_health_issues     BOOLEAN NOT NULL DEFAULT FALSE,
    health_notes          TEXT,
    last_donation_date    DATE,
    prediction_source     VARCHAR(20) DEFAULT 'MANUAL',
    prediction_confidence NUMERIC(4,3),
    is_available          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_donors_group_city ON donors (blood_group, city);
CREATE INDEX idx_donors_last_donation ON donors (last_donation_date);

CREATE TABLE donation_history (
    id          BIGSERIAL PRIMARY KEY,
    donor_id    BIGINT NOT NULL REFERENCES donors(id) ON DELETE CASCADE,
    donated_on  DATE NOT NULL,
    location    VARCHAR(150),
    units       SMALLINT NOT NULL DEFAULT 1
);

-- Block Supabase's public REST API from reading these tables.
-- The Spring Boot backend connects as the postgres role, which bypasses RLS.
ALTER TABLE donors ENABLE ROW LEVEL SECURITY;
ALTER TABLE donation_history ENABLE ROW LEVEL SECURITY;
