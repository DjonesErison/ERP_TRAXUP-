ALTER TABLE trials_saas ADD COLUMN onboarding_concluido_em TIMESTAMPTZ NULL;
CREATE INDEX idx_trial_onboarding_tenant ON trials_saas (tenant_id, onboarding_status);
