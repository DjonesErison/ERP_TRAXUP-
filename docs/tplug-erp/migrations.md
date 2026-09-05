# Migrations Flyway — TPlug ERP

As migrations abaixo já pertencem ao histórico oficial do backend e são imutáveis. Qualquer alteração futura deve entrar em uma nova migration.

## V1 — Estrutura inicial de tenant, empresa e filial

Cria `tenants`, `empresas` e `filiais`, com UUIDs, timestamps, relacionamentos e índices iniciais.

## V2 — Reforço de integridade multi-tenant

Adiciona chave única composta em Empresa e FK composta em Filial para impedir associação de filial a empresa de outro tenant. Também adiciona índice `(tenant_id, empresa_id)`.

## V3 — Usuários por tenant

Cria `usuarios` com UUID, tenant, nome, e-mail, hash de senha, ativo e timestamps. E-mail é único por tenant.

## V4 — Refresh tokens

Adiciona a chave composta necessária em usuários e cria `refresh_tokens`, vinculados de forma tenant-safe a usuários. O token é persistido somente por hash.

## V5 — RBAC

Cria:

- `permissoes`;
- `perfis`;
- `usuario_perfis`;
- `perfil_permissoes`.

As associações relevantes usam constraints compostas para preservar isolamento por tenant.

## V6 — Catálogo fundamental de permissões

Insere as permissões iniciais de Empresa, Filial e `RBAC_GERENCIAR`, com UUIDs determinísticos e `ON CONFLICT (chave) DO NOTHING`.

## V7 — Auditoria multi-tenant

Cria a estrutura persistente de eventos de auditoria com identificação de tenant, usuário, empresa, filial, operação, entidade, entidade afetada, detalhes e timestamp, além dos índices necessários para consulta por tenant e entidade.

## Regras para novas migrations

- Não editar V1–V7.
- Próxima alteração de banco: V8.
- Migrations devem executar do zero em banco limpo no CI.
- Hibernate permanece com `ddl-auto=validate`; o Flyway é o dono da evolução do schema.
- Toda constraint multi-tenant nova deve ser analisada também no nível do banco, não apenas na aplicação.
