# Migrations Flyway — TPlug ERP

As migrations abaixo pertencem ao histórico oficial do backend e são imutáveis. Qualquer alteração futura deve entrar em uma nova migration.

## Inventário atual

- `V1`–`V50` — fundação multi-tenant, autenticação/RBAC/auditoria, catálogo, estoque, pessoas, compras, vendas e financeiro.
- `V51`–`V55` — Produto Combo, escolhas, snapshots e vigência.
- `V56`–`V58` — CRM de retorno, follow-up e interações.
- `V59`–`V61` — inventário, ajuste explícito e contagem cega.
- `V62` — terminais de PDV tenant-safe, código estável, série única por filial, estado ativo/inativo e permissões RBAC próprias.
- `V63` — registro tenant-safe e idempotente de operações locais de venda do PDV, com chave por terminal/operação, série derivada do terminal, número local único e checksum SHA-256.
- `V64` — vínculo tenant-safe e idempotente entre o ACK da venda offline do PDV e um único `PedidoVenda` em rascunho.
- `V65` — identidade idempotente dos itens da venda offline, com UUID local por item, unicidade por ACK e FK composta garantindo isolamento por tenant.
- `V66` — permissões RBAC específicas de pós-venda do PDV para leitura, segunda via/reimpressão, cancelamento e alteração de pagamento.
- `V67` — configuração operacional tenant-safe por terminal: regras de cancelamento, tamanho/destinos de impressão e permissões RBAC próprias.

## Regras para novas migrations

- Não editar migrations já aplicadas (`V1`–`V67`).
- A próxima migration deve usar `V68`.
- Migrations devem executar do zero em banco limpo no CI.
- Hibernate permanece com `ddl-auto=validate`; o Flyway é o dono da evolução do schema e do catálogo RBAC versionado.
- Toda constraint multi-tenant nova deve ser analisada também no nível do banco, não apenas na aplicação.
- FKs que representam domínio tenant-scoped devem preferir chaves compostas que incluam `tenant_id` e, quando a regra exigir, `filial_id`.

A TRAXUP Central não compartilha este histórico Flyway e permanece separada do runtime do TPlug ERP.
