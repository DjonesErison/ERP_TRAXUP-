ALTER TABLE trials_saas ADD COLUMN admin_ativado_em TIMESTAMPTZ;
-- Trials already activated before email delivery was introduced remain activated.
UPDATE trials_saas t SET admin_ativado_em = a.usado_em
FROM (SELECT usuario_id, MIN(usado_em) usado_em FROM ativacao_admin_tokens
      WHERE usado_em IS NOT NULL GROUP BY usuario_id) a
WHERE t.administrador_id = a.usuario_id;

CREATE TABLE trial_activation_emails (
    trial_id UUID PRIMARY KEY REFERENCES trials_saas(id),
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_requested_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    window_started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    requests_in_window INTEGER NOT NULL DEFAULT 1,
    sent_at TIMESTAMPTZ,
    last_error VARCHAR(40)
);
CREATE INDEX idx_trial_email_pending ON trial_activation_emails (next_attempt_at)
    WHERE status IN ('PENDING','SENDING');
