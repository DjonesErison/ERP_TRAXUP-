# Fase 2 — Compras

A Fase 2 inicia o fluxo operacional de compras do TPlug ERP sobre a fundacao multi-tenant, RBAC, auditoria e cadastros concluidos nas fases anteriores.

## Blocos

- [x] Cabecalho do pedido de compra.
- [x] Itens do pedido de compra.
- [x] Recebimento de compras.
- [x] Integracao do recebimento com estoque.
- [x] Cancelamento, estados e endurecimento final do fluxo.

## Pedidos e itens

O pedido de compra pertence a um tenant e a uma filial, referencia um fornecedor ativo do mesmo tenant e nasce em estado RASCUNHO. Enquanto estiver em RASCUNHO, pode receber itens do tipo PRODUTO ou GRADE, com quantidade e preco unitario. O total de cada item e calculado no backend.

## Estados e cancelamento

As transicoes de estado que alteram o pedido usam bloqueio pessimista por `tenant + id`, evitando corrida entre abertura, cancelamento e recebimento. O pedido so pode ser aberto a partir de `RASCUNHO` e quando possui itens. Pedido `RECEBIDO` nao pode ser cancelado; cancelar novamente um pedido ja `CANCELADO` gera conflito explicito. Regras invalidas usam as excecoes padronizadas da API e as mutacoes continuam auditadas.

## Recebimento e estoque

O recebimento e registrado para pedido ABERTO do mesmo tenant, copia os itens do pedido com as quantidades efetivamente recebidas e impede mais de um recebimento para o mesmo pedido. O pedido e bloqueado pessimisticamente durante o registro para serializar operacoes concorrentes.

A integracao com estoque usa o mesmo tenant e a filial do recebimento, movimenta PRODUTO ou GRADE como ENTRADA e ocorre na mesma transacao da mudanca de estado do recebimento e do pedido. Operacoes invalidas usam as excecoes padronizadas de regra de negocio/conflito e a trilha de auditoria permanece ativa.

A Fase 2 fica consolidada com o fluxo base de compras, recebimento e entrada em estoque. Incrementos futuros de compras devem entrar como novas capacidades de negocio, e nao como pendencias desta fase base.

A TRAXUP Central permanece separada e sem alteracao de runtime nesta fase.
