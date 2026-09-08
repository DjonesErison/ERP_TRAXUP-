ALTER TABLE crm_cliente_followups
    ADD CONSTRAINT uq_crm_cliente_followups_tenant_id UNIQUE (tenant_id, id);

CREATE TABLE crm_cliente_interacoes (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    filial_id UUID NOT NULL,
    cliente_id UUID NOT NULL,
    followup_id UUID,
    canal VARCHAR(20) NOT NULL,
    resultado VARCHAR(30) NOT NULL,
    assunto VARCHAR(160) NOT NULL,
    ocorrido_em TIMESTAMPTZ NOT NULL,
    usuario_id UUID,
    criado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_crm_cliente_interacoes_canal CHECK (canal IN ('TELEFONE','EMAIL','WHATSAPP','PRESENCIAL','OUTRO')),
    CONSTRAINT ck_crm_cliente_interacoes_resultado CHECK (resultado IN ('CONTATO_REALIZADO','SEM_RETORNO','INTERESSE','SEM_INTERESSE','OUTRO')),
    CONSTRAINT fk_crm_interacao_tenant_filial FOREIGN KEY (tenant_id, filial_id)
        REFERENCES filiais (tenant_id, id),
    CONSTRAINT fk_crm_interacao_tenant_cliente FOREIGN KEY (tenant_id, cliente_id)
        REFERENCES pessoas (tenant_id, id),
    CONSTRAINT fk_crm_interacao_tenant_followup FOREIGN KEY (tenant_id, followup_id)
        REFERENCES crm_cliente_followups (tenant_id, id),
    CONSTRAINT fk_crm_interacao_tenant_usuario FOREIGN KEY (tenant_id, usuario_id)
        REFERENCES usuarios (tenant_id, id)
);

CREATE INDEX idx_crm_interacoes_tenant_cliente_ocorrido
    ON crm_cliente_interacoes (tenant_id, cliente_id, ocorrido_em DESC, id);
CREATE INDEX idx_crm_interacoes_tenant_filial_ocorrido
    ON crm_cliente_interacoes (tenant_id, filial_id, ocorrido_em DESC, id);
CREATE INDEX idx_crm_interacoes_tenant_canal_resultado
    ON crm_cliente_interacoes (tenant_id, canal, resultado, ocorrido_em DESC);
