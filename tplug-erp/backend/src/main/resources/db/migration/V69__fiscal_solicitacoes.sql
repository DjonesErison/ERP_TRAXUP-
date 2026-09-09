ALTER TABLE pedidos_venda
    ADD CONSTRAINT uq_pedidos_venda_tenant_id UNIQUE (tenant_id, id);

CREATE TABLE fiscal_solicitacoes (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    filial_id UUID NOT NULL,
    pedido_venda_id UUID NOT NULL,
    modelo VARCHAR(10) NOT NULL,
    ambiente VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_fiscal_solicitacoes_modelo CHECK (modelo IN ('NFCE', 'NFE')),
    CONSTRAINT ck_fiscal_solicitacoes_ambiente CHECK (ambiente IN ('HOMOLOGACAO', 'PRODUCAO')),
    CONSTRAINT ck_fiscal_solicitacoes_status CHECK (status IN ('PENDENTE', 'PROCESSANDO', 'AUTORIZADO', 'REJEITADO', 'CONTINGENCIA', 'CANCELADO')),
    CONSTRAINT uq_fiscal_solicitacoes_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_fiscal_solicitacoes_venda UNIQUE (tenant_id, pedido_venda_id, modelo, ambiente),
    CONSTRAINT fk_fiscal_solicitacoes_tenant_filial FOREIGN KEY (tenant_id, filial_id)
        REFERENCES filiais (tenant_id, id),
    CONSTRAINT fk_fiscal_solicitacoes_tenant_venda FOREIGN KEY (tenant_id, pedido_venda_id)
        REFERENCES pedidos_venda (tenant_id, id)
);

CREATE INDEX idx_fiscal_solicitacoes_tenant_status_criado
    ON fiscal_solicitacoes (tenant_id, status, criado_em DESC);

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000077', 'FISCAL_DOCUMENTO_LER', 'Consultar solicitacoes e documentos fiscais'),
    ('10000000-0000-0000-0000-000000000078', 'FISCAL_DOCUMENTO_EMITIR', 'Solicitar emissao fiscal a partir de venda faturada')
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
JOIN permissoes perm ON perm.chave IN ('FISCAL_DOCUMENTO_LER', 'FISCAL_DOCUMENTO_EMITIR')
WHERE UPPER(p.nome) = 'ADMIN'
ON CONFLICT DO NOTHING;
