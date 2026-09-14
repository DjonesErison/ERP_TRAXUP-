ALTER TABLE contabilidade_sped_exportacoes
    ADD COLUMN filial_id UUID,
    ADD COLUMN legado_sem_filial BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE contabilidade_sped_exportacoes e
SET filial_id = unica.filial_id
FROM (
    SELECT tenant_id, MIN(id) AS filial_id
    FROM filiais
    WHERE ativo = TRUE
    GROUP BY tenant_id
    HAVING COUNT(*) = 1
) unica
WHERE unica.tenant_id = e.tenant_id
  AND e.filial_id IS NULL;

UPDATE contabilidade_sped_exportacoes
SET legado_sem_filial = TRUE
WHERE filial_id IS NULL;

ALTER TABLE contabilidade_sped_exportacoes
    ADD CONSTRAINT fk_sped_exportacao_tenant_filial
        FOREIGN KEY (tenant_id, filial_id)
        REFERENCES filiais (tenant_id, id),
    ADD CONSTRAINT ck_sped_exportacao_escopo_filial CHECK (
        (filial_id IS NOT NULL AND legado_sem_filial = FALSE)
        OR (filial_id IS NULL AND legado_sem_filial = TRUE)
    );

ALTER TABLE contabilidade_sped_exportacoes
    DROP CONSTRAINT uq_sped_exportacao_competencia;

ALTER TABLE contabilidade_sped_exportacoes
    ADD CONSTRAINT uq_sped_exportacao_competencia_filial
        UNIQUE NULLS NOT DISTINCT (
            tenant_id, filial_id, tipo, competencia
        );

CREATE INDEX idx_sped_exportacoes_filial_competencia
    ON contabilidade_sped_exportacoes (
        tenant_id, filial_id, competencia DESC, tipo
    );

COMMENT ON COLUMN contabilidade_sped_exportacoes.filial_id IS
    'Filial proprietaria da exportacao SPED';
COMMENT ON COLUMN contabilidade_sped_exportacoes.legado_sem_filial IS
    'Marca registros anteriores a V102 sem filial determinavel';
