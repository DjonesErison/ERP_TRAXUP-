CREATE TABLE pessoa_contatos (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    pessoa_id UUID NOT NULL REFERENCES pessoas(id),
    nome VARCHAR(120) NOT NULL,
    cargo VARCHAR(120),
    email VARCHAR(255),
    telefone VARCHAR(30),
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_pessoa_contatos_tenant_pessoa ON pessoa_contatos (tenant_id, pessoa_id);

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000028', 'PESSOA_CONTATO_LER', 'Consultar contatos de clientes e fornecedores'),
    ('10000000-0000-0000-0000-000000000029', 'PESSOA_CONTATO_GERENCIAR', 'Gerenciar contatos de clientes e fornecedores')
ON CONFLICT (chave) DO NOTHING;
