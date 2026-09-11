ALTER TABLE contabilidade_sped_exportacoes
    ADD COLUMN tentativas_processamento INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN versao_layout VARCHAR(40),
    ADD CONSTRAINT ck_sped_exportacao_tentativas
        CHECK (tentativas_processamento >= 0),
    ADD CONSTRAINT ck_sped_exportacao_versao_layout
        CHECK (versao_layout IS NULL OR status = 'CONCLUIDO');

CREATE INDEX idx_sped_exportacoes_worker
    ON contabilidade_sped_exportacoes (
        tenant_id, tentativas_processamento, atualizado_em
    )
    WHERE status IN ('PENDENTE', 'PROCESSANDO', 'FALHOU');
