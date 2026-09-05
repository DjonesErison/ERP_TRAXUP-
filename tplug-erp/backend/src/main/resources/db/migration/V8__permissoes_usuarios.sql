INSERT INTO permissoes (id, chave, descricao)
VALUES
    ('10000000-0000-0000-0000-000000000008', 'USUARIO_LER', 'Permite consultar usuarios do tenant'),
    ('10000000-0000-0000-0000-000000000009', 'USUARIO_CRIAR', 'Permite criar usuarios no tenant'),
    ('10000000-0000-0000-0000-000000000010', 'USUARIO_DESATIVAR', 'Permite desativar usuarios no tenant')
ON CONFLICT (chave) DO NOTHING;
