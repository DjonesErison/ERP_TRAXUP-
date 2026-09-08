ALTER TABLE pessoas
    ADD CONSTRAINT uq_pessoas_tenant_id UNIQUE (tenant_id, id);

CREATE TABLE crm_cliente_followups (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    filial_id UUID NOT NULL,
    cliente_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    assunto VARCHAR(160) NOT NULL,
    observacao VARCHAR(500),
    agendado_para TIMESTAMPTZ NOT NULL,
    criado_por_id UUID,
    concluido_por_id UUID,
    concluido_em TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_crm_cliente_followups_status CHECK (status IN ('PENDENTE','CONCLUIDO','CANCELADO')),
    CONSTRAINT fk_crm_followup_tenant_filial FOREIGN KEY (tenant_id, filial_id)
        REFERENCES filiais (tenant_id, id),
    CONSTRAINT fk_crm_followup_tenant_cliente FOREIGN KEY (tenant_id, cliente_id)
        REFERENCES pessoas (tenant_id, id),
    CONSTRAINT fk_crm_followup_tenant_criado_por FOREIGN KEY (tenant_id, criado_por_id)
        REFERENCES usuarios (tenant_id, id),
    CONSTRAINT fk_crm_followup_tenant_concluido_por FOREIGN KEY (tenant_id, concluido_por_id)
        REFERENCES usuarios (tenant_id, id)
);

CREATE INDEX idx_crm_followups_tenant_status_agenda
    ON crm_cliente_followups (tenant_id, status, agendado_para, id);
CREATE INDEX idx_crm_followups_tenant_cliente
    ON crm_cliente_followups (tenant_id, cliente_id, agendado_para DESC);
CREATE INDEX idx_crm_followups_tenant_filial_status
    ON crm_cliente_followups (tenant_id, filial_id, status, agendado_para);

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000062', 'CRM_CLIENTE_RETORNO_EDITAR', 'Criar e atualizar follow-ups de retorno de clientes')
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
JOIN permissoes perm ON perm.chave = 'CRM_CLIENTE_RETORNO_EDITAR'
WHERE UPPER(p.nome) = 'ADMIN'
ON CONFLICT DO NOTHING;
