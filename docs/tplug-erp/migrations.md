# Migrations Flyway — TPlug ERP

As migrations abaixo pertencem ao histórico oficial do backend e são imutáveis. Qualquer alteração futura deve entrar em uma nova migration.

## Inventário atual

- `V1` — estrutura inicial de tenant, empresa e filial.
- `V2` — reforço de integridade multi-tenant de empresa e filial.
- `V3` — usuários por tenant.
- `V4` — refresh tokens.
- `V5` — perfis e permissões RBAC.
- `V6` — catálogo de permissões fundamentais.
- `V7` — fundação de auditoria multi-tenant.
- `V8` — permissões de usuários.
- `V9` — permissão de leitura de auditoria.
- `V10` — produtos.
- `V11` — grades de produto.
- `V12` — saldos de estoque.
- `V13` — movimentações de estoque.
- `V14` — pessoas.
- `V15` — endereços de pessoas.
- `V16` — contatos de pessoas.
- `V17` — sincronização de permissões do perfil ADMIN.
- `V18` — pedidos de compra.
- `V19` — itens de pedidos de compra.
- `V20` — recebimentos de compra.
- `V21` — integração do recebimento de compra com estoque.
- `V22` — pedidos de venda.
- `V23` — itens de pedidos de venda.
- `V24` — desconto em itens de pedido de venda.
- `V25` — contas a receber.
- `V26` — hardening de contatos de pessoas.
- `V27` — recebimentos parciais de contas a receber.
- `V28` — contas a pagar.
- `V29` — hardening dos recebimentos financeiros.
- `V30` — contas financeiras e caixa.
- `V31` — formas e condições de pagamento.
- `V32` — forma e condição de pagamento nos pedidos de venda.
- `V33` — conciliação financeira.
- `V34` — hardening da conciliação financeira.
- `V35` — classificação de eventos de conciliação.
- `V36` — integrações financeiras.
- `V37` — checkpoint de integrações financeiras.
- `V38` — observabilidade das integrações financeiras.
- `V39` — pagamentos parciais de contas a pagar.
- `V40` — ajustes comerciais das condições de pagamento.
- `V41` — origem idempotente das contas a receber.
- `V42` — origem idempotente dos movimentos financeiros.
- `V43` — índices operacionais de títulos financeiros.
- `V44` — integridade de filial nos históricos de baixas financeiras.
- `V45` — integridade tenant-safe do usuário nos históricos de baixas financeiras.
- `V46` — integridade tenant-safe do usuário nos lançamentos de conciliação financeira, preservando registros históricos sem usuário.
- `V47` — integridade tenant-safe das referências de integrações financeiras, vinculando conta à filial correta e usuário ao mesmo tenant.
- `V48` — integridade tenant-safe das contas financeiras e movimentos de tesouraria, vinculando usuário ao tenant e movimento à conta da filial correta.
- `V49` — integridade tenant-safe do usuário nos títulos de contas a receber e contas a pagar, preservando registros históricos sem usuário.
- `V50` — integridade da conta financeira com a filial nos lançamentos de conciliação, inclusive enquanto pendentes.
- `V51` — Produto Combo fixo, com componentes e quantidades tenant-scoped.
- `V52` — grupos de escolha e opções configuráveis do Produto Combo.
- `V53` — seleção de opções do Produto Combo nos itens do pedido de venda.
- `V54` — snapshots de produto, quantidade e valor adicional das opções escolhidas para refletir corretamente preço e estoque da venda.
- `V55` — vigência opcional do Produto Combo, com limites inclusivos, validação de intervalo e vínculo tenant-safe ao produto.
- `V56` — permissão RBAC para consulta de clientes inativos no CRM de retorno.

## Regras para novas migrations

- Não editar migrations já aplicadas (`V1`–`V56`).
- A próxima migration deve usar `V57`.
- Migrations devem executar do zero em banco limpo no CI.
- Hibernate permanece com `ddl-auto=validate`; o Flyway é o dono da evolução do schema e do catálogo RBAC versionado.
- Toda constraint multi-tenant nova deve ser analisada também no nível do banco, não apenas na aplicação.
- FKs que representam domínio tenant-scoped devem preferir chaves compostas que incluam `tenant_id` e, quando a regra exigir, `filial_id`.

A TRAXUP Central não compartilha este histórico Flyway e permanece separada do runtime do TPlug ERP.
