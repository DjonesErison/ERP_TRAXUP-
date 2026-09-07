# Vendas - periodo na consulta recente

Este incremento evolui `GET /api/v1/vendas/pedidos/recentes` com filtros opcionais por periodo, preservando o comportamento atual quando eles nao forem informados.

## Novos filtros opcionais

- `inicio`: instante inicial inclusivo em ISO-8601.
- `fim`: instante final inclusivo em ISO-8601.
- Quando ambos forem informados, `inicio` nao pode ser posterior a `fim`.

Os filtros existentes `filialId`, `status` e `limite` continuam validos. A consulta permanece sempre escopada por `tenantId`, exige `VENDA_PEDIDO_LER` e preserva ordenacao deterministica por `criado_em DESC, id ASC`.

Nao ha migration, auditoria de leitura, alteracao fiscal, mudanca em cancelamento pos-faturamento, pagamento ou runtime da TRAXUP Central.
