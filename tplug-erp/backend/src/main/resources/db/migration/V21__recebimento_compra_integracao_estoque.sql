INSERT INTO permissoes (id, chave, descricao)
VALUES ('10000000-0000-0000-0000-000000000035', 'COMPRA_RECEBIMENTO_INTEGRAR_ESTOQUE', 'Integrar recebimento de compra ao estoque')
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
JOIN permissoes perm ON perm.chave = 'COMPRA_RECEBIMENTO_INTEGRAR_ESTOQUE'
WHERE UPPER(p.nome) = 'ADMIN'
ON CONFLICT DO NOTHING;
