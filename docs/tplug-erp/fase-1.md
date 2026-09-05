# Fase 1 — Cadastros operacionais

A Fase 1 inicia a camada funcional do TPlug ERP sobre a fundação multi-tenant, segurança, RBAC e auditoria concluída na Fase 0.

## Blocos

- [x] Cadastro mestre de produtos.
- [ ] Grades e variações de produtos.
- [ ] Estrutura inicial de estoque por filial e item.
- [ ] Movimentações de estoque.
- [ ] Fornecedores e clientes.
- [ ] Compras e vendas.

## Grades de produtos

O bloco de grades permite associar variações a um produto base, com código próprio, descrição, código de barras e preço de venda opcional. Toda operação é isolada pelo tenant autenticado, protegida por RBAC e auditada nas operações críticas.

Este documento trata exclusivamente do TPlug ERP. A TRAXUP Central permanece separada e sem alteração de runtime nesta fase.
