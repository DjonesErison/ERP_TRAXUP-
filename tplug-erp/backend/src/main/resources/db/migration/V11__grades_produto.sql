CREATE TABLE grades_produto (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    produto_id UUID NOT NULL REFERENCES produtos(id),
    codigo_grade VARCHAR(60) NOT NULL,
    descricao_grade VARCHAR(255) NOT NULL,
    codigo_barra VARCHAR(60),
    venda_prc NUMERIC(19,4),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_grade_tenant_codigo UNIQUE (tenant_id, codigo_grade)
);

CREATE INDEX idx_grades_produto_tenant_produto ON grades_produto (tenant_id, produto_id);
CREATE INDEX idx_grades_produto_tenant_codigo_barra ON grades_produto (tenant_id, codigo_barra);

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000015', 'GRADE_PRODUTO_LER', 'Consultar grades de produtos'),
    ('10000000-0000-0000-0000-000000000016', 'GRADE_PRODUTO_CRIAR', 'Criar grades de produtos'),
    ('10000000-0000-0000-0000-000000000017', 'GRADE_PRODUTO_DESATIVAR', 'Desativar grades de produtos')
ON CONFLICT (chave) DO NOTHING;
