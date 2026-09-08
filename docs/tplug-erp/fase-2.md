# Fase 2 — Compras

A Fase 2 inicia o fluxo operacional de compras do TPlug ERP sobre a fundacao multi-tenant, RBAC, auditoria e cadastros concluidos nas fases anteriores.

## Blocos

- [x] Cabecalho do pedido de compra.
- [x] Itens do pedido de compra.
- [x] Recebimento de compras.
- [x] Integracao do recebimento com estoque.
- [ ] Cancelamento, estados e endurecimento final do fluxo.

## Pedidos e itens

O pedido de compra pertence a um tenant e a uma filial, referencia um fornecedor ativo do mesmo tenant e nasce em estado RASCUNHO. Enquanto estiver em RASCUNHO, pode receber itens do tipo PRODUTO ou GRADE, com quantidade e preco unitario. O total de cada item e calculado no backend.

## Recebimento e estoque

O recebimento e registrado para pedido ABERTO do mesmo tenant, copia os itens do pedido com as quantidades efetivamente recebidas e impede mais de um recebimento para o mesmo pedido. O pedido e bloqueado pessimisticamente durante o registro para serializar operacoes concorrentes.

A integracao com estoque usa o mesmo tenant e a filial do recebimento, movimenta PRODUTO ou GRADE como ENTRADA e ocorre na mesma transacao da mudanca de estado do recebimento e do pedido. Operacoes invalidas usam as excecoes padronizadas de regra de negocio/conflito e a trilha de auditoria permanece ativa.

A TRAXUP Central permanece separada e sem alteracao de runtime nesta fase.
