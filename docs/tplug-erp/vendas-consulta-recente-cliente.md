# Vendas - cliente na consulta recente

Este incremento evolui `GET /api/v1/vendas/pedidos/recentes` com filtro opcional por cliente.

## Novo filtro opcional

- `clienteId`: UUID do cliente associado ao pedido.

O filtro e combinado com `filialId`, `status`, `inicio`, `fim` e `limite`. A consulta continua obrigatoriamente escopada pelo `tenantId` obtido do contexto autenticado, exige `VENDA_PEDIDO_LER` e preserva ordenacao deterministica por `criado_em DESC, id ASC`.

Nao ha consulta de cliente fora do tenant, migration, auditoria de leitura, alteracao fiscal, mudanca de regras de faturamento/cancelamento/pagamento ou impacto no runtime da TRAXUP Central.
