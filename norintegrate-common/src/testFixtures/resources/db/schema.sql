-- =============================================================================
-- NorIntegrate Database Schema
-- PostgreSQL 18+
-- =============================================================================

CREATE TABLE visa_type (
    id          VARCHAR(64)  NOT NULL,
    name        VARCHAR(128) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_visa_type PRIMARY KEY (id)
);;

CREATE TABLE procedure (
    id             BIGINT       GENERATED ALWAYS AS IDENTITY,
    title          VARCHAR(255) NOT NULL,
    description    TEXT,
    authority      VARCHAR(128),
    estimated_days INT,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_procedure PRIMARY KEY (id)
);;

CREATE TABLE procedure_dependency (
    prerequisite_id  BIGINT NOT NULL,
    dependent_id     BIGINT NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_procedure_dependency       PRIMARY KEY (prerequisite_id, dependent_id),
    CONSTRAINT fk_proc_dep_prerequisite      FOREIGN KEY (prerequisite_id) REFERENCES procedure (id) ON DELETE CASCADE,
    CONSTRAINT fk_proc_dep_dependent         FOREIGN KEY (dependent_id)    REFERENCES procedure (id) ON DELETE CASCADE,
    CONSTRAINT chk_no_self_dependency        CHECK (prerequisite_id <> dependent_id)
);;

CREATE TABLE document_requirement (
    id           BIGINT       GENERATED ALWAYS AS IDENTITY,
    procedure_id BIGINT       NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    description  TEXT,
    is_mandatory BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_document_requirement    PRIMARY KEY (id),
    CONSTRAINT fk_doc_req_procedure       FOREIGN KEY (procedure_id) REFERENCES procedure (id) ON DELETE CASCADE
);;

CREATE TABLE checklist_template (
    id            BIGINT      GENERATED ALWAYS AS IDENTITY,
    visa_type_id  VARCHAR(64) NOT NULL,
    procedure_id  BIGINT      NOT NULL,
    display_order INT         NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_checklist_template      PRIMARY KEY (id),
    CONSTRAINT uq_checklist_template_item UNIQUE (visa_type_id, procedure_id),
    CONSTRAINT fk_checklist_visa_type     FOREIGN KEY (visa_type_id) REFERENCES visa_type (id)  ON DELETE CASCADE,
    CONSTRAINT fk_checklist_procedure     FOREIGN KEY (procedure_id) REFERENCES procedure (id) ON DELETE CASCADE
);;

CREATE TABLE app_user (
    id             UUID         NOT NULL DEFAULT gen_random_uuid(),
    oauth_provider VARCHAR(64)  NOT NULL,
    oauth_subject  VARCHAR(255) NOT NULL,
    email          VARCHAR(255) NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_app_user               PRIMARY KEY (id),
    CONSTRAINT uq_app_user_oauth         UNIQUE (oauth_provider, oauth_subject)
);;

CREATE TABLE user_progress (
    id           BIGINT      GENERATED ALWAYS AS IDENTITY,
    user_id      UUID        NOT NULL,
    procedure_id BIGINT      NOT NULL,
    completed    BOOLEAN     NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_user_progress          PRIMARY KEY (id),
    CONSTRAINT uq_user_progress_item     UNIQUE (user_id, procedure_id),
    CONSTRAINT fk_user_progress_user     FOREIGN KEY (user_id)      REFERENCES app_user (id)  ON DELETE CASCADE,
    CONSTRAINT fk_user_progress_proc     FOREIGN KEY (procedure_id) REFERENCES procedure (id) ON DELETE CASCADE
);;

CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;;

CREATE TRIGGER trg_procedure_updated_at
    BEFORE UPDATE ON procedure
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();;

CREATE TRIGGER trg_user_progress_updated_at
    BEFORE UPDATE ON user_progress
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();;
