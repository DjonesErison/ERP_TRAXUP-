INSERT INTO permissoes (id, chave, descricao) VALUES
    (
        '89000000-0000-4000-8000-000000000001',
        'FISCAL_REPOSITORIO_CONTABILIDADE_LER',
        'Consultar, baixar e exportar documentos do repositorio fiscal para a contabilidade'
    )
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
JOIN permissoes perm
  ON perm.chave = 'FISCAL_REPOSITORIO_CONTABILIDADE_LER'
WHERE UPPER(p.nome) = 'ADMIN'
ON CONFLICT DO NOTHING;
