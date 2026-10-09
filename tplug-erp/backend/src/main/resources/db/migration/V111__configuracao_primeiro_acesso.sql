CREATE TABLE onboarding_configuracoes (
 tenant_id UUID PRIMARY KEY REFERENCES tenants(id),
 cep VARCHAR(9), endereco VARCHAR(200), numero VARCHAR(20), complemento VARCHAR(100),
 bairro VARCHAR(100), cidade VARCHAR(100), uf VARCHAR(2),
 empresa_revisada BOOLEAN NOT NULL DEFAULT FALSE,
 equipe_revisada BOOLEAN NOT NULL DEFAULT FALSE,
 atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);
