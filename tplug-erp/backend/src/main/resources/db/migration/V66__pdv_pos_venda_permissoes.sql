INSERT INTO permissoes (id, chave, descricao) VALUES
    ('10000000-0000-0000-0000-000000000069', 'PDV_VENDA_LER', 'Consultar ultimas vendas e detalhes no PDV'),
    ('10000000-0000-0000-0000-000000000070', 'PDV_VENDA_REIMPRIMIR', 'Emitir segunda via ou reimpressao de comprovante no PDV'),
    ('10000000-0000-0000-0000-000000000071', 'PDV_VENDA_CANCELAR', 'Cancelar venda elegivel pelo PDV'),
    ('10000000-0000-0000-0000-000000000072', 'PDV_VENDA_ALTERAR_PAGAMENTO', 'Alterar pagamento de venda elegivel pelo PDV')
ON CONFLICT (chave) DO NOTHING;

INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
SELECT p.tenant_id, p.id, perm.id
FROM perfis p
JOIN permissoes perm ON perm.chave IN (
    'PDV_VENDA_LER', 'PDV_VENDA_REIMPRIMIR', 'PDV_VENDA_CANCELAR', 'PDV_VENDA_ALTERAR_PAGAMENTO'
)
WHERE UPPER(p.nome) = 'ADMIN'
ON CONFLICT DO NOTHING;
