CREATE TABLE pessoa_enderecos (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    pessoa_id UUID NOT NULL REFERENCES pessoas(id),
    tipo VARCHAR(20) NOT NULL,
    logradouro VARCHAR(255) NOT NULL,
    numero VARCHAR(30),
    complemento VARCHAR(120),
    bairro VARCHAR(120),
    cidade VARCHAR(120) NOT NULL,
    uf VARCHAR(2) NOT NULL,
    cep VARCHAR(8),
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_pessoa_endereco_tipo CHECK (tipo IN ('PRINCIPAL', 'COBRANCA', 'ENTREGA', 'OUTRO'))
);

CREATE INDEX idx_pessoa_enderecos_tenant_pessoa ON pessoa_enderecos (tenant_id, pessoa_id);

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000024', 'PESSOA_ENDERECO_LER', 'Consultar enderecos de pessoas'),
    ('10000000-0000-0000-0000-000000000025', 'PESSOA_ENDERECO_GERENCIAR', 'Gerenciar enderecos de pessoas')
ON CONFLICT (chave) DO NOTHING;
