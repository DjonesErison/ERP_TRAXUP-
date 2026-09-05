INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
CROSS JOIN permissoes perm
WHERE UPPER(p.nome) = 'ADMIN'
ON CONFLICT (tenant_id, perfil_id, permissao_id) DO NOTHING;
