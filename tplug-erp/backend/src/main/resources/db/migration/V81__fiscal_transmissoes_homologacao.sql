CREATE TABLE fiscal_transmissoes (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    solicitacao_id UUID NOT NULL,
    documento_id UUID NOT NULL,
    assinatura_id UUID NOT NULL,
    ambiente VARCHAR(20) NOT NULL,
    provedor VARCHAR(40) NOT NULL,
    status VARCHAR(30) NOT NULL,
    codigo_resposta VARCHAR(10) NOT NULL,
    mensagem_resposta VARCHAR(500) NOT NULL,
    protocolo VARCHAR(80) NOT NULL,
    hash_requisicao CHAR(64) NOT NULL,
    hash_resposta CHAR(64) NOT NULL,
    transmitido_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_fiscal_transmissao_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_fiscal_transmissao_assinatura UNIQUE (tenant_id, assinatura_id),
    CONSTRAINT uq_fiscal_transmissao_protocolo UNIQUE (tenant_id, protocolo),
    CONSTRAINT fk_fiscal_transmissao_solicitacao FOREIGN KEY (tenant_id, solicitacao_id)
        REFERENCES fiscal_solicitacoes (tenant_id, id),
    CONSTRAINT fk_fiscal_transmissao_documento FOREIGN KEY (tenant_id, documento_id)
        REFERENCES fiscal_documentos (tenant_id, id),
    CONSTRAINT fk_fiscal_transmissao_assinatura FOREIGN KEY (tenant_id, assinatura_id)
        REFERENCES fiscal_documentos_assinaturas (tenant_id, id),
    CONSTRAINT ck_fiscal_transmissao_ambiente CHECK (ambiente = 'HOMOLOGACAO'),
    CONSTRAINT ck_fiscal_transmissao_provedor CHECK (provedor = 'SIMULADO'),
    CONSTRAINT ck_fiscal_transmissao_status CHECK (status = 'AUTORIZADO_SIMULADO'),
    CONSTRAINT ck_fiscal_transmissao_codigo CHECK (codigo_resposta = '100-SIM'),
    CONSTRAINT ck_fiscal_transmissao_mensagem CHECK (length(btrim(mensagem_resposta)) > 0),
    CONSTRAINT ck_fiscal_transmissao_protocolo CHECK (
        protocolo ~ '^SIM-[0-9a-f]{32}$'
    ),
    CONSTRAINT ck_fiscal_transmissao_hash_requisicao CHECK (
        hash_requisicao ~ '^[0-9a-f]{64}$'
    ),
    CONSTRAINT ck_fiscal_transmissao_hash_resposta CHECK (
        hash_resposta ~ '^[0-9a-f]{64}$'
    )
);

CREATE INDEX idx_fiscal_transmissoes_tenant_data
    ON fiscal_transmissoes (tenant_id, transmitido_em DESC);

COMMENT ON TABLE fiscal_transmissoes IS
    'Transmissoes fiscais; V81 aceita somente autorizacao simulada em homologacao';

COMMENT ON COLUMN fiscal_transmissoes.protocolo IS
    'Protocolo artificial prefixado com SIM; nao representa autorizacao da SEFAZ';

COMMENT ON COLUMN fiscal_transmissoes.status IS
    'AUTORIZADO_SIMULADO nao deve alterar a solicitacao para AUTORIZADO';
