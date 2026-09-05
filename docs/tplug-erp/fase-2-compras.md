# Fase 2 - Compras

## Fluxo operacional

1. O pedido de compra nasce em `RASCUNHO`.
2. Itens podem ser incluidos somente enquanto o pedido estiver em `RASCUNHO`.
3. O pedido so pode ser aberto quando possuir ao menos um item.
4. Um pedido `ABERTO` pode receber conferencia de mercadoria.
5. O recebimento nasce em `CONFERIDO` e ainda nao altera estoque.
6. A integracao do recebimento gera movimentos de `ENTRADA` por produto/grade e marca o recebimento como `INTEGRADO_ESTOQUE`.
7. A mesma transacao conclui o pedido como `RECEBIDO`.
8. A integracao nao pode ser repetida.

## Estados do pedido

- `RASCUNHO`: permite manutencao de itens.
- `ABERTO`: pedido finalizado para recebimento.
- `CANCELADO`: fluxo encerrado sem recebimento.
- `RECEBIDO`: recebimento integrado ao estoque.

## Seguranca e consistencia

- Tenant sempre derivado do JWT/TenantContext.
- Filial, fornecedor, produtos e grades sao validados no tenant.
- Endpoints protegidos por RBAC.
- Abertura, cancelamento, recebimento e integracao possuem trilha de auditoria.
- Recebimento e integracao com estoque usam transacoes de aplicacao.
- A TRAXUP Central permanece fora do runtime do TPlug ERP.

## Proxima fase

Fase 3: vendas, pedidos de venda, itens, descontos, cancelamento e baixa de estoque.
