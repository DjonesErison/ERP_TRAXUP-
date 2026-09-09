CREATE TABLE fiscal_tentativas_emissao (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    solicitacao_id UUID NOT NULL,
    documento_id UUID NOT NULL,
    rejeicao_id UUID NOT NULL,
    correcao_id UUID NOT NULL,
    tentativa_anterior_id UUID,
    numero INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'CRIADA',
    hash_documento_corrigido CHAR(64) NOT NULL,
    criada_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    iniciada_em TIMESTAMPTZ,
    concluida_em TIMESTAMPTZ,
    CONSTRAINT uq_fiscal_tentativa_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_fiscal_tentativa_numero UNIQUE (tenant_id, documento_id, numero),
    CONSTRAINT uq_fiscal_tentativa_correcao UNIQUE (tenant_id, correcao_id),
    CONSTRAINT fk_fiscal_tentativa_solicitacao FOREIGN KEY (tenant_id, solicitacao_id)
        REFERENCES fiscal_solicitacoes (tenant_id, id),
    CONSTRAINT fk_fiscal_tentativa_documento FOREIGN KEY (tenant_id, documento_id)
        REFERENCES fiscal_documentos (tenant_id, id),
    CONSTRAINT fk_fiscal_tentativa_rejeicao FOREIGN KEY (tenant_id, rejeicao_id)
        REFERENCES fiscal_rejeicoes (tenant_id, id),
    CONSTRAINT fk_fiscal_tentativa_correcao FOREIGN KEY (tenant_id, correcao_id)
        REFERENCES fiscal_rejeicao_correcoes (tenant_id, id),
    CONSTRAINT fk_fiscal_tentativa_anterior FOREIGN KEY (tenant_id, tentativa_anterior_id)
        REFERENCES fiscal_tentativas_emissao (tenant_id, id),
    CONSTRAINT ck_fiscal_tentativa_numero CHECK (numero > 0),
    CONSTRAINT ck_fiscal_tentativa_status CHECK (
        status IN ('CRIADA', 'EM_PROCESSAMENTO', 'CONCLUIDA', 'FALHOU')
    ),
    CONSTRAINT ck_fiscal_tentativa_hash CHECK (
        hash_documento_corrigido ~ '^[0-9a-f]{64}$'
    ),
    CONSTRAINT ck_fiscal_tentativa_tempos CHECK (
        (status = 'CRIADA' AND iniciada_em IS NULL AND concluida_em IS NULL)
        OR (status = 'EM_PROCESSAMENTO' AND iniciada_em IS NOT NULL AND concluida_em IS NULL)
        OR (status IN ('CONCLUIDA', 'FALHOU')
            AND iniciada_em IS NOT NULL AND concluida_em IS NOT NULL)
    )
);

CREATE TABLE fiscal_tentativa_artefatos_anteriores (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    tentativa_id UUID NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    xml_id UUID,
    assinatura_id UUID,
    transmissao_id UUID,
    processado_id UUID,
    situacao VARCHAR(40) NOT NULL,
    registrado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_fiscal_tentativa_artefato_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_fiscal_tentativa_artefato_tipo UNIQUE (tenant_id, tentativa_id, tipo),
    CONSTRAINT fk_fiscal_tentativa_artefato_tentativa FOREIGN KEY (tenant_id, tentativa_id)
        REFERENCES fiscal_tentativas_emissao (tenant_id, id),
    CONSTRAINT fk_fiscal_tentativa_artefato_xml FOREIGN KEY (tenant_id, xml_id)
        REFERENCES fiscal_documentos_xml (tenant_id, id),
    CONSTRAINT fk_fiscal_tentativa_artefato_assinatura FOREIGN KEY (tenant_id, assinatura_id)
        REFERENCES fiscal_documentos_assinaturas (tenant_id, id),
    CONSTRAINT fk_fiscal_tentativa_artefato_transmissao FOREIGN KEY (tenant_id, transmissao_id)
        REFERENCES fiscal_transmissoes (tenant_id, id),
    CONSTRAINT fk_fiscal_tentativa_artefato_processado FOREIGN KEY (tenant_id, processado_id)
        REFERENCES fiscal_documentos_processados (tenant_id, id),
    CONSTRAINT ck_fiscal_tentativa_artefato_tipo CHECK (
        tipo IN ('XML', 'ASSINATURA', 'TRANSMISSAO', 'PROCESSADO')
    ),
    CONSTRAINT ck_fiscal_tentativa_artefato_unico CHECK (
        num_nonnulls(xml_id, assinatura_id, transmissao_id, processado_id) = 1
        AND (tipo <> 'XML' OR xml_id IS NOT NULL)
        AND (tipo <> 'ASSINATURA' OR assinatura_id IS NOT NULL)
        AND (tipo <> 'TRANSMISSAO' OR transmissao_id IS NOT NULL)
        AND (tipo <> 'PROCESSADO' OR processado_id IS NOT NULL)
    ),
    CONSTRAINT ck_fiscal_tentativa_artefato_situacao CHECK (
        situacao = 'INVALIDADO_POR_CORRECAO'
    )
);

CREATE INDEX idx_fiscal_tentativas_documento
    ON fiscal_tentativas_emissao (tenant_id, documento_id, numero DESC);

CREATE INDEX idx_fiscal_tentativa_artefatos
    ON fiscal_tentativa_artefatos_anteriores (tenant_id, tentativa_id);

COMMENT ON TABLE fiscal_tentativas_emissao IS
    'Novas tentativas rastreaveis abertas somente a partir de correcao fiscal aplicada';

COMMENT ON TABLE fiscal_tentativa_artefatos_anteriores IS
    'Vinculos historicos dos artefatos superados; os registros originais nunca sao apagados';

COMMENT ON COLUMN fiscal_tentativa_artefatos_anteriores.situacao IS
    'Invalidacao logica para reemissao; nao apaga nem altera o artefato fiscal original';
