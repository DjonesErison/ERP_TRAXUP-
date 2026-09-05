CREATE TABLE pessoas (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    tipo_pessoa VARCHAR(10) NOT NULL,
    nome_razao_social VARCHAR(255) NOT NULL,
    nome_fantasia VARCHAR(255),
    cpf_cnpj VARCHAR(14),
    email VARCHAR(255),
    telefone VARCHAR(30),
    cliente BOOLEAN NOT NULL DEFAULT FALSE,
    fornecedor BOOLEAN NOT NULL DEFAULT FALSE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_pessoas_tipo CHECK (tipo_pessoa IN ('FISICA', 'JURIDICA')),
    CONSTRAINT ck_pessoas_papel CHECK (cliente OR fornecedor),
    CONSTRAINT ck_pessoas_documento CHECK (cpf_cnpj IS NULL OR char_length(cpf_cnpj) IN (11,14)),
    CONSTRAINT uk_pessoas_tenant_documento UNIQUE (tenant_id, cpf_cnpj)
);

CREATE INDEX idx_pessoas_tenant_nome ON pessoas (tenant_id, nome_razao_social);
CREATE INDEX idx_pessoas_tenant_cliente ON pessoas (tenant_id, cliente);
CREATE INDEX idx_pessoas_tenant_fornecedor ON pessoas (tenant_id, fornecedor);

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000021', 'PESSOA_LER', 'Consultar clientes e fornecedores'),
    ('10000000-0000-0000-0000-000000000022', 'PESSOA_CRIAR', 'Criar clientes e fornecedores'),
    ('10000000-0000-0000-0000-000000000023', 'PESSOA_DESATIVAR', 'Desativar clientes e fornecedores')
ON CONFLICT (chave) DO NOTHING;
