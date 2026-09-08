CREATE INDEX idx_pedidos_venda_crm_retorno
    ON pedidos_venda (tenant_id, cliente_id, criado_em DESC)
    WHERE status = 'FATURADO';

INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000061', 'CRM_CLIENTE_RETORNO_LER', 'Consultar clientes inativos para campanhas de retorno')
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
JOIN permissoes perm ON perm.chave = 'CRM_CLIENTE_RETORNO_LER'
WHERE UPPER(p.nome) = 'ADMIN'
ON CONFLICT DO NOTHING;
