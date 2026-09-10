INSERT INTO perfis (id, tenant_id, nome, descricao, ativo)
SELECT gen_random_uuid(), t.id, 'CONTABILIDADE',
       'Acesso independente e somente leitura ao repositorio fiscal', TRUE
FROM tenants t
WHERE NOT EXISTS (
    SELECT 1
    FROM perfis p
    WHERE p.tenant_id = t.id
      AND UPPER(p.nome) = 'CONTABILIDADE'
);

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
JOIN permissoes perm
  ON perm.chave = 'FISCAL_REPOSITORIO_CONTABILIDADE_LER'
WHERE UPPER(p.nome) = 'CONTABILIDADE'
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
            'Acesso independente e somente leitura ao repositorio fiscal',
            TRUE
        );
    END IF;

    INSERT INTO perfil_permissoes (
        tenant_id, perfil_id, permissao_id
    )
    SELECT NEW.id, v_perfil_id, perm.id
    FROM permissoes perm
    WHERE perm.chave = 'FISCAL_REPOSITORIO_CONTABILIDADE_LER'
    ON CONFLICT DO NOTHING;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_tenant_perfil_contabilidade
AFTER INSERT ON tenants
FOR EACH ROW
EXECUTE FUNCTION provisionar_perfil_contabilidade_tenant();
