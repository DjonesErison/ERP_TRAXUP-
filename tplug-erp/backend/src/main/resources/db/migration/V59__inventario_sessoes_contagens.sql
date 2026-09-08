CREATE TABLE inventario_sessoes (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    filial_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    descricao VARCHAR(160),
    criado_por_id UUID,
    concluido_por_id UUID,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    concluido_em TIMESTAMPTZ,
    CONSTRAINT ck_inventario_sessoes_status CHECK (status IN ('ABERTO','CONCLUIDO','CANCELADO')),
    CONSTRAINT uq_inventario_sessoes_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT fk_inventario_sessao_tenant_filial FOREIGN KEY (tenant_id, filial_id)
        REFERENCES filiais (tenant_id, id),
    CONSTRAINT fk_inventario_sessao_tenant_criado_por FOREIGN KEY (tenant_id, criado_por_id)
        REFERENCES usuarios (tenant_id, id),
    CONSTRAINT fk_inventario_sessao_tenant_concluido_por FOREIGN KEY (tenant_id, concluido_por_id)
        REFERENCES usuarios (tenant_id, id)
);

CREATE TABLE inventario_contagens (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    inventario_id UUID NOT NULL,
    tipo_item VARCHAR(10) NOT NULL,
    item_id UUID NOT NULL,
    quantidade_sistema NUMERIC(19,4) NOT NULL,
    quantidade_contada NUMERIC(19,4) NOT NULL,
    divergencia NUMERIC(19,4) NOT NULL,
    contado_por_id UUID,
    contado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_inventario_contagens_tipo_item CHECK (tipo_item IN ('PRODUTO','GRADE')),
    CONSTRAINT ck_inventario_contagens_quantidade CHECK (quantidade_contada >= 0),
    CONSTRAINT uq_inventario_contagem_item UNIQUE (tenant_id, inventario_id, tipo_item, item_id),
    CONSTRAINT fk_inventario_contagem_tenant_sessao FOREIGN KEY (tenant_id, inventario_id)
        REFERENCES inventario_sessoes (tenant_id, id),
    CONSTRAINT fk_inventario_contagem_tenant_usuario FOREIGN KEY (tenant_id, contado_por_id)
        REFERENCES usuarios (tenant_id, id)
);

CREATE INDEX idx_inventario_sessoes_tenant_filial_status
    ON inventario_sessoes (tenant_id, filial_id, status, criado_em DESC);
CREATE INDEX idx_inventario_contagens_tenant_sessao
    ON inventario_contagens (tenant_id, inventario_id, tipo_item, item_id);
CREATE INDEX idx_inventario_contagens_divergencia
    ON inventario_contagens (tenant_id, inventario_id, divergencia)
    WHERE divergencia <> 0;

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000063', 'INVENTARIO_LER', 'Consultar sessoes e contagens de inventario'),
    ('10000000-0000-0000-0000-000000000064', 'INVENTARIO_EDITAR', 'Criar, contar, concluir e cancelar inventarios')
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
JOIN permissoes perm ON perm.chave IN ('INVENTARIO_LER', 'INVENTARIO_EDITAR')
WHERE UPPER(p.nome) = 'ADMIN'
ON CONFLICT DO NOTHING;
