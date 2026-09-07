# Vendas - filtros da consulta recente

Este incremento evolui `GET /api/v1/vendas/pedidos/recentes` sem alterar o comportamento existente quando filtros nao sao informados.

## Filtros opcionais

- `filialId`: restringe a consulta a uma filial dentro do tenant autenticado.
- `status`: aceita `RASCUNHO`, `ABERTO`, `FATURADO` ou `CANCELADO`, sem diferenciar maiusculas/minusculas e ignorando espacos externos.
- `limite`: permanece com padrao 20 e faixa de 1 a 100.

A consulta continua sempre escopada por `tenantId`, exige `VENDA_PEDIDO_LER` e preserva ordenacao deterministica por `criado_em DESC, id ASC`.

Nao ha migration, auditoria de leitura, alteracao fiscal, mudanca em cancelamento pos-faturamento, pagamento ou runtime da TRAXUP Central.
