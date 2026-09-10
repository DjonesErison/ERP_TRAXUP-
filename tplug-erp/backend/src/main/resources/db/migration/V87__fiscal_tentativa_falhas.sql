CREATE TABLE fiscal_tentativa_falhas (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    tentativa_id UUID NOT NULL,
    etapa VARCHAR(20) NOT NULL,
    tipo_erro VARCHAR(120) NOT NULL,
    ocorrida_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolvida_em TIMESTAMPTZ,
    CONSTRAINT uq_fiscal_tentativa_falha_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT fk_fiscal_tentativa_falha_tentativa
        FOREIGN KEY (tenant_id, tentativa_id)
        REFERENCES fiscal_tentativas_emissao (tenant_id, id),
    CONSTRAINT ck_fiscal_tentativa_falha_etapa CHECK (
        etapa IN ('XML', 'ASSINATURA', 'TRANSMISSAO', 'PROCESSADO')
    ),
    CONSTRAINT ck_fiscal_tentativa_falha_tipo CHECK (
        length(btrim(tipo_erro)) > 0
    )
);

CREATE INDEX idx_fiscal_tentativa_falhas_abertas
    ON fiscal_tentativa_falhas (tenant_id, tentativa_id, ocorrida_em DESC)
    WHERE resolvida_em IS NULL;

COMMENT ON TABLE fiscal_tentativa_falhas IS
    'Falhas tecnicas append-only do fluxo de reemissao; nao armazena XML, payload ou mensagem sensivel';

COMMENT ON COLUMN fiscal_tentativa_falhas.tipo_erro IS
    'Somente o nome tecnico da classe da excecao, sem mensagem ou dados fiscais';
