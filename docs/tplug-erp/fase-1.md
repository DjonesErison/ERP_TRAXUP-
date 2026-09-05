# Fase 1 — Cadastros operacionais

A Fase 1 inicia a camada funcional do TPlug ERP sobre a fundação multi-tenant, segurança, RBAC e auditoria concluída na Fase 0.

## Blocos

- [x] Cadastro mestre de produtos.
- [x] Grades e variações de produtos.
- [x] Estrutura inicial de estoque por filial e item.
- [x] Movimentações de estoque.
- [x] Fornecedores e clientes.
- [x] Endereços de clientes e fornecedores.
- [x] Contatos de clientes e fornecedores.
- [ ] Compras e recebimento.
- [ ] Vendas e pedidos.

## Entregas consolidadas

A fase já dispõe de cadastro mestre de produtos e grades, saldos e movimentações de estoque por filial e item, além do cadastro unificado de pessoas para clientes e fornecedores com endereços e contatos associados.

As operações usam o tenant autenticado como fronteira de dados e são protegidas por permissões RBAC específicas. As próximas entregas entram no fluxo transacional de compras/recebimento e, em seguida, vendas/pedidos.

Este documento trata exclusivamente do TPlug ERP. A TRAXUP Central permanece separada e sem alteração de runtime nesta fase.
