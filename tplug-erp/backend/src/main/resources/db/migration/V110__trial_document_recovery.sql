-- Existing duplicates must be resolved operationally before this migration; never delete customer data here.
UPDATE trials_saas SET documento=regexp_replace(documento, '[^0-9]', '', 'g');
ALTER TABLE trials_saas ADD CONSTRAINT ck_trial_documento_normalizado CHECK (documento ~ '^([0-9]{11}|[0-9]{14})$');
ALTER TABLE trials_saas ADD CONSTRAINT uq_trial_documento UNIQUE (documento);
CREATE TABLE access_recovery_emails (
    usuario_id UUID PRIMARY KEY REFERENCES usuarios(id),
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_requested_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    window_started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    requests_in_window INTEGER NOT NULL DEFAULT 1
);
