-- V1__Initial_Schema.sql
-- Script initial pour créer le schéma de CandiFlow

CREATE TYPE user_role AS ENUM ('ADMIN', 'CANDIDATE', 'RECRUITER');
CREATE TYPE job_status AS ENUM ('OPEN', 'CLOSED');

CREATE TABLE users
(
    user_id       UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255)        NOT NULL,
    role          user_role           NOT NULL,
    name          VARCHAR(255),
    created_at    TIMESTAMPTZ         NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ         NOT NULL DEFAULT now()
);
CREATE INDEX idx_users_email ON users (email);

CREATE TABLE applications
(
    application_id UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id        UUID         NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    company_name   VARCHAR(255) NOT NULL,
    job_title      VARCHAR(255) NOT NULL,
    job_url        TEXT,
    date_applied   TIMESTAMPTZ  NOT NULL,
    follow_up_date DATE,
    general_notes  TEXT,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_applications_user_id ON applications (user_id);

CREATE TABLE application_statuses
(
    status_id     UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    name          VARCHAR(100) UNIQUE NOT NULL,
    description   TEXT,
    display_order INTEGER,
    icon_name     VARCHAR(50),
    is_active     BOOLEAN             NOT NULL DEFAULT true,
    created_at    TIMESTAMPTZ         NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ         NOT NULL DEFAULT now()
);
CREATE INDEX idx_application_statuses_name ON application_statuses (name);
CREATE INDEX idx_application_statuses_display_order ON application_statuses (display_order);

CREATE TABLE status_updates
(
    status_update_id UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    application_id   UUID        NOT NULL REFERENCES applications (application_id) ON DELETE CASCADE,
    status_id        UUID        NOT NULL REFERENCES application_statuses (status_id) ON DELETE RESTRICT,
    event_date       TIMESTAMPTZ NOT NULL,
    notes            TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_status_updates_application_id ON status_updates (application_id);
CREATE INDEX idx_status_updates_status_id ON status_updates (status_id);
CREATE INDEX idx_status_updates_event_date ON status_updates (event_date);

CREATE TABLE documents
(
    document_id    UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    application_id UUID         NOT NULL REFERENCES applications (application_id) ON DELETE CASCADE,
    file_name      VARCHAR(255) NOT NULL,
    storage_path   TEXT         NOT NULL,
    file_type      VARCHAR(100),
    file_size      BIGINT,
    uploaded_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_documents_application_id ON documents (application_id);

CREATE TABLE job_openings
(
    job_opening_id    UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    recruiter_user_id UUID         NOT NULL REFERENCES users (user_id) ON DELETE SET NULL,
    title             VARCHAR(255) NOT NULL,
    description       TEXT,
    status            job_status   NOT NULL DEFAULT 'OPEN',
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_job_openings_recruiter_user_id ON job_openings (recruiter_user_id);
CREATE INDEX idx_job_openings_status ON job_openings (status);

CREATE TABLE pipeline_stages
(
    stage_id      UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    name          VARCHAR(100) UNIQUE NOT NULL,
    description   TEXT,
    display_order INTEGER UNIQUE      NOT NULL,
    is_end_stage  BOOLEAN             NOT NULL DEFAULT false,
    is_active     BOOLEAN             NOT NULL DEFAULT true,
    created_at    TIMESTAMPTZ         NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ         NOT NULL DEFAULT now()
);
CREATE INDEX idx_pipeline_stages_name ON pipeline_stages (name);
CREATE INDEX idx_pipeline_stages_display_order ON pipeline_stages (display_order);

CREATE TABLE candidate_sources
(
    source_id   UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    name        VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    is_active   BOOLEAN             NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ         NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ         NOT NULL DEFAULT now()
);
CREATE INDEX idx_candidate_sources_name ON candidate_sources (name);

CREATE TABLE opening_applicants
(
    applicant_id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    job_opening_id            UUID         NOT NULL REFERENCES job_openings (job_opening_id) ON DELETE CASCADE,
    current_stage_id          UUID         NOT NULL REFERENCES pipeline_stages (stage_id) ON DELETE RESTRICT,
    source_id                 UUID         REFERENCES candidate_sources (source_id) ON DELETE SET NULL,
    name                      VARCHAR(255) NOT NULL,
    email                     VARCHAR(255),
    phone                     VARCHAR(50),
    cv_storage_path           TEXT,
    cover_letter_storage_path TEXT,
    application_date          DATE                  DEFAULT CURRENT_DATE,
    created_at                TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_opening_applicants_job_opening_id ON opening_applicants (job_opening_id);
CREATE INDEX idx_opening_applicants_current_stage_id ON opening_applicants (current_stage_id);
CREATE INDEX idx_opening_applicants_source_id ON opening_applicants (source_id);
CREATE INDEX idx_opening_applicants_email ON opening_applicants (email);

CREATE TABLE tags
(
    tag_id     UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    name       VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMPTZ         NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ         NOT NULL DEFAULT now()
);
CREATE INDEX idx_tags_name ON tags (name);

CREATE TABLE application_tags
(
    application_id UUID NOT NULL REFERENCES applications (application_id) ON DELETE CASCADE,
    tag_id         UUID NOT NULL REFERENCES tags (tag_id) ON DELETE CASCADE,
    PRIMARY KEY (application_id, tag_id)
);
CREATE INDEX idx_application_tags_tag_id ON application_tags (tag_id);

CREATE TABLE applicant_tags
(
    applicant_id UUID NOT NULL REFERENCES opening_applicants (applicant_id) ON DELETE CASCADE,
    tag_id       UUID NOT NULL REFERENCES tags (tag_id) ON DELETE CASCADE,
    PRIMARY KEY (applicant_id, tag_id)
);
CREATE INDEX idx_applicant_tags_tag_id ON applicant_tags (tag_id);

CREATE TABLE recruiter_notes
(
    note_id        UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    applicant_id   UUID        NOT NULL REFERENCES opening_applicants (applicant_id) ON DELETE CASCADE,
    author_user_id UUID        NOT NULL REFERENCES users (user_id) ON DELETE SET NULL,
    note_text      TEXT        NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_recruiter_notes_applicant_id ON recruiter_notes (applicant_id);
CREATE INDEX idx_recruiter_notes_author_user_id ON recruiter_notes (author_user_id);

CREATE OR REPLACE FUNCTION trigger_set_timestamp()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_timestamp_users
    BEFORE UPDATE
    ON users
    FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();
CREATE TRIGGER set_timestamp_applications
    BEFORE UPDATE
    ON applications
    FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();
CREATE TRIGGER set_timestamp_job_openings
    BEFORE UPDATE
    ON job_openings
    FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();
CREATE TRIGGER set_timestamp_opening_applicants
    BEFORE UPDATE
    ON opening_applicants
    FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();