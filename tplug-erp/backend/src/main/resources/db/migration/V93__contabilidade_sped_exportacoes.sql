CREATE TABLE contabilidade_sped_exportacoes (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    tipo VARCHAR(30) NOT NULL,
    competencia DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    solicitado_por_id UUID,
    chave_objeto VARCHAR(500),
    hash_sha256 CHAR(64),
    erro_codigo VARCHAR(80),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    concluido_em TIMESTAMPTZ,
    CONSTRAINT uq_sped_exportacao_tenant_id UNIQUE (tenant_id, id),
    CONSTRAINT uq_sped_exportacao_competencia
        UNIQUE (tenant_id, tipo, competencia),
    CONSTRAINT fk_sped_exportacao_tenant_usuario
        FOREIGN KEY (tenant_id, solicitado_por_id)
        REFERENCES usuarios (tenant_id, id),
    CONSTRAINT ck_sped_exportacao_tipo CHECK (
        tipo IN ('EFD_ICMS_IPI', 'EFD_CONTRIBUICOES')
    ),
    CONSTRAINT ck_sped_exportacao_competencia CHECK (
        EXTRACT(DAY FROM competencia) = 1
    ),
    CONSTRAINT ck_sped_exportacao_status CHECK (
        status IN ('PENDENTE', 'PROCESSANDO', 'CONCLUIDO', 'FALHOU')
    ),
    CONSTRAINT ck_sped_exportacao_hash CHECK (
        hash_sha256 IS NULL OR hash_sha256 ~ '^[0-9a-f]{64}$'
    ),
    CONSTRAINT ck_sped_exportacao_resultado CHECK (
        (
            status = 'CONCLUIDO'
            AND chave_objeto IS NOT NULL
            AND hash_sha256 IS NOT NULL
            AND concluido_em IS NOT NULL
        )
        OR (
            status <> 'CONCLUIDO'
            AND chave_objeto IS NULL
            AND hash_sha256 IS NULL
            AND concluido_em IS NULL
        )
    ),
    CONSTRAINT ck_sped_exportacao_erro CHECK (
        erro_codigo IS NULL OR status = 'FALHOU'
    )
);

CREATE INDEX idx_sped_exportacoes_pendentes
    ON contabilidade_sped_exportacoes (tenant_id, criado_em)
    WHERE status IN ('PENDENTE', 'FALHOU');

CREATE INDEX idx_sped_exportacoes_competencia
    ON contabilidade_sped_exportacoes (
        tenant_id, competencia DESC, tipo
    );

INSERT INTO permissoes (id, chave, descricao) VALUES
    (
        '93000000-0000-4000-8000-000000000001',
        'CONTABILIDADE_SPED_SOLICITAR',
        'Solicitar e acompanhar exportacoes SPED'
    )
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
JOIN permissoes perm
  ON perm.chave = 'CONTABILIDADE_SPED_SOLICITAR'
WHERE UPPER(p.nome) IN ('ADMIN', 'CONTABILIDADE')
ON CONFLICT DO NOTHING;

CREATE OR REPLACE FUNCTION provisionar_perfil_contabilidade_tenant()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_perfil_id UUID;
BEGIN
    SELECT p.id
      INTO v_perfil_id
      FROM perfis p
     WHERE p.tenant_id = NEW.id
       AND UPPER(p.nome) = 'CONTABILIDADE'
     ORDER BY p.criado_em
     LIMIT 1;

    IF v_perfil_id IS NULL THEN
        v_perfil_id := gen_random_uuid();
        INSERT INTO perfis (
            id, tenant_id, nome, descricao, ativo
        ) VALUES (
            v_perfil_id,
            NEW.id,
            'CONTABILIDADE',
            'Acesso independente e somente leitura a dados contabeis',
            TRUE
        );
    END IF;

    INSERT INTO perfil_permissoes (
        tenant_id, perfil_id, permissao_id
    )
    SELECT NEW.id, v_perfil_id, perm.id
    FROM permissoes perm
    WHERE perm.chave IN (
        'FISCAL_REPOSITORIO_CONTABILIDADE_LER',
        'CONTABILIDADE_LIVRO_CAIXA_LER',
        'CONTABILIDADE_INVENTARIO_LER',
        'CONTABILIDADE_SPED_SOLICITAR'
    )
    ON CONFLICT DO NOTHING;

    RETURN NEW;
END;
$$;
