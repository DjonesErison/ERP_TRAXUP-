INSERT INTO permissoes (id, chave, descricao) VALUES
    (
        '96000000-0000-4000-8000-000000000001',
        'CONTABILIDADE_SPED_REPROCESSAR',
        'Reprocessar exportacoes SPED com falha'
    )
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
JOIN permissoes perm
  ON perm.chave = 'CONTABILIDADE_SPED_REPROCESSAR'
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
        'CONTABILIDADE_SPED_SOLICITAR',
        'CONTABILIDADE_SPED_BAIXAR',
        'CONTABILIDADE_SPED_REPROCESSAR'
    )
    ON CONFLICT DO NOTHING;

    RETURN NEW;
END;
$$;
