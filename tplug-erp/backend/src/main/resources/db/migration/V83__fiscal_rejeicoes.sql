CREATE TABLE fiscal_rejeicoes (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    solicitacao_id UUID NOT NULL,
    documento_id UUID NOT NULL,
    transmissao_id UUID,
    ambiente VARCHAR(20) NOT NULL,
    origem VARCHAR(30) NOT NULL,
    codigo VARCHAR(20) NOT NULL,
    mensagem VARCHAR(500) NOT NULL,
    categoria VARCHAR(30) NOT NULL,
    corrigivel BOOLEAN NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ABERTA',
    campos_correcao JSONB NOT NULL DEFAULT '[]'::jsonb,
    detectada_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolvida_em TIMESTAMPTZ,
    CONSTRAINT uq_fiscal_rejeicao_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_fiscal_rejeicao_evento UNIQUE (
        tenant_id, documento_id, origem, codigo, detectada_em
    ),
    CONSTRAINT fk_fiscal_rejeicao_solicitacao FOREIGN KEY (tenant_id, solicitacao_id)
        REFERENCES fiscal_solicitacoes (tenant_id, id),
    CONSTRAINT fk_fiscal_rejeicao_documento FOREIGN KEY (tenant_id, documento_id)
        REFERENCES fiscal_documentos (tenant_id, id),
    CONSTRAINT fk_fiscal_rejeicao_transmissao FOREIGN KEY (tenant_id, transmissao_id)
        REFERENCES fiscal_transmissoes (tenant_id, id),
    CONSTRAINT ck_fiscal_rejeicao_ambiente CHECK (
        ambiente IN ('HOMOLOGACAO', 'PRODUCAO')
    ),
    CONSTRAINT ck_fiscal_rejeicao_origem CHECK (
        origem IN ('VALIDACAO_LOCAL', 'ASSINATURA', 'TRANSMISSAO', 'SEFAZ')
    ),
    CONSTRAINT ck_fiscal_rejeicao_codigo CHECK (length(btrim(codigo)) > 0),
    CONSTRAINT ck_fiscal_rejeicao_mensagem CHECK (length(btrim(mensagem)) > 0),
    CONSTRAINT ck_fiscal_rejeicao_categoria CHECK (
        categoria IN (
            'CADASTRO', 'TRIBUTACAO', 'NUMERACAO', 'CERTIFICADO',
            'ASSINATURA', 'COMUNICACAO', 'AUTORIZADOR', 'NAO_CLASSIFICADA'
        )
    ),
    CONSTRAINT ck_fiscal_rejeicao_status CHECK (
        status IN ('ABERTA', 'EM_CORRECAO', 'CORRIGIDA', 'IGNORADA')
    ),
    CONSTRAINT ck_fiscal_rejeicao_campos_array CHECK (
        jsonb_typeof(campos_correcao) = 'array'
    ),
    CONSTRAINT ck_fiscal_rejeicao_campos_corrigiveis CHECK (
        corrigivel OR campos_correcao = '[]'::jsonb
    ),
    CONSTRAINT ck_fiscal_rejeicao_resolucao CHECK (
        (status IN ('ABERTA', 'EM_CORRECAO') AND resolvida_em IS NULL)
        OR (status IN ('CORRIGIDA', 'IGNORADA') AND resolvida_em IS NOT NULL)
    )
);

CREATE INDEX idx_fiscal_rejeicoes_abertas
    ON fiscal_rejeicoes (tenant_id, documento_id, detectada_em DESC)
    WHERE status IN ('ABERTA', 'EM_CORRECAO');

CREATE INDEX idx_fiscal_rejeicoes_tenant_data
    ON fiscal_rejeicoes (tenant_id, detectada_em DESC);

COMMENT ON TABLE fiscal_rejeicoes IS
    'Historico imutavel da deteccao de rejeicoes; correcoes devem ser registradas separadamente';

COMMENT ON COLUMN fiscal_rejeicoes.campos_correcao IS
    'Lista de nomes de campos permitidos para orientar a interface; nunca armazenar valores sensiveis';

COMMENT ON COLUMN fiscal_rejeicoes.transmissao_id IS
    'Opcional para rejeicoes detectadas antes da transmissao';
