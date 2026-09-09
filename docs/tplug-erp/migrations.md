# Migrations Flyway — TPlug ERP

As migrations abaixo pertencem ao histórico oficial do backend e são imutáveis. Qualquer alteração futura deve entrar em uma nova migration.

## Inventário atual

- `V1`–`V50` — fundação multi-tenant, autenticação/RBAC/auditoria, catálogo, estoque, pessoas, compras, vendas e financeiro.
- `V51`–`V55` — Produto Combo, escolhas, snapshots e vigência.
- `V56`–`V58` — CRM de retorno, follow-up e interações.
- `V59`–`V61` — inventário, ajuste explícito e contagem cega.
- `V62` — terminais de PDV tenant-safe, código estável, série única por filial, estado ativo/inativo e permissões RBAC próprias.
- `V63` — registro tenant-safe e idempotente de operações locais de venda do PDV.
- `V64` — vínculo tenant-safe entre ACK do PDV e `PedidoVenda`.
- `V65` — identidade idempotente dos itens da venda offline.
- `V66` — permissões RBAC específicas de pós-venda do PDV.
- `V67` — configuração operacional tenant-safe por terminal.
- `V68` — sessões de caixa tenant-safe por terminal, vinculadas a conta financeira do tipo CAIXA, com saldo de abertura, saldo de fechamento, diferença e permissões RBAC próprias.
- `V69` — contrato persistido e idempotente entre venda faturada e módulo fiscal, separado por modelo NFCE/NFE e ambiente HOMOLOGACAO/PRODUCAO, com RBAC fiscal próprio.
- `V70` — snapshot imutável e tenant-safe dos itens da venda para preparação do documento fiscal.
- `V71` — consolidação idempotente do documento fiscal estruturado com totais congelados.
- `V72` — XML fiscal preparatório interno, versionado, íntegro por SHA-256 e restrito à homologação.

## Regras para novas migrations

- Não editar migrations já aplicadas (`V1`–`V72`).
- A próxima migration deve usar `V73`.
- Migrations devem executar do zero em banco limpo no CI.
- Hibernate permanece com `ddl-auto=validate`; o Flyway é o dono da evolução do schema e do catálogo RBAC versionado.
- Toda constraint multi-tenant nova deve ser analisada também no nível do banco, não apenas na aplicação.
- FKs que representam domínio tenant-scoped devem preferir chaves compostas que incluam `tenant_id` e, quando a regra exigir, `filial_id`.

A TRAXUP Central não compartilha este histórico Flyway e permanece separada do runtime do TPlug ERP.
