# Fase 2 — Compras

A Fase 2 inicia o fluxo operacional de compras do TPlug ERP sobre a fundacao multi-tenant, RBAC, auditoria e cadastros concluidos nas fases anteriores.

## Blocos

- [x] Cabecalho do pedido de compra.
- [x] Itens do pedido de compra.
- [ ] Recebimento de compras.
- [ ] Integracao do recebimento com estoque.
- [ ] Cancelamento, estados e endurecimento do fluxo.

## Pedidos e itens

O pedido de compra pertence a um tenant e a uma filial, referencia um fornecedor ativo do mesmo tenant e nasce em estado RASCUNHO. Enquanto estiver em RASCUNHO, pode receber itens do tipo PRODUTO ou GRADE, com quantidade e preco unitario. O total de cada item e calculado no backend.

A TRAXUP Central permanece separada e sem alteracao de runtime nesta fase.
