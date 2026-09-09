CREATE TABLE pdv_configuracoes (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    filial_id UUID NOT NULL,
    terminal_id UUID NOT NULL,
    exigir_justificativa_cancelamento BOOLEAN NOT NULL DEFAULT TRUE,
    exigir_autorizacao_cancelamento BOOLEAN NOT NULL DEFAULT FALSE,
    tamanho_impressao VARCHAR(16) NOT NULL DEFAULT 'MEDIA',
    imprimir_caixa BOOLEAN NOT NULL DEFAULT TRUE,
    imprimir_cozinha BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_pdv_config_tamanho_impressao CHECK (tamanho_impressao IN ('PEQUENA', 'MEDIA', 'GRANDE')),
    CONSTRAINT uq_pdv_config_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_pdv_config_terminal UNIQUE (tenant_id, terminal_id),
    CONSTRAINT fk_pdv_config_tenant_filial FOREIGN KEY (tenant_id, filial_id)
        REFERENCES filiais (tenant_id, id),
    CONSTRAINT fk_pdv_config_tenant_terminal FOREIGN KEY (tenant_id, terminal_id)
        REFERENCES pdv_terminais (tenant_id, id)
);

CREATE INDEX idx_pdv_config_tenant_filial
    ON pdv_configuracoes (tenant_id, filial_id);

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000073', 'PDV_CONFIGURACAO_LER', 'Consultar configuracao operacional do PDV'),
    ('10000000-0000-0000-0000-000000000074', 'PDV_CONFIGURACAO_GERENCIAR', 'Alterar configuracao operacional do PDV')
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
JOIN permissoes perm ON perm.chave IN ('PDV_CONFIGURACAO_LER', 'PDV_CONFIGURACAO_GERENCIAR')
WHERE UPPER(p.nome) = 'ADMIN'
ON CONFLICT DO NOTHING;
