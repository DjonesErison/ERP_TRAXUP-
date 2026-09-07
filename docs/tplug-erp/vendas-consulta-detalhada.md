# Vendas - Consulta detalhada

Este incremento evolui o requisito de Ultimas Vendas / Consulta de Vendas com uma visao read-only consolidada de um pedido.

## Escopo implementado

- `GET /api/v1/vendas/pedidos/{pedidoId}/detalhe` retorna cabecalho, itens e totais do pedido.
- O tenant continua sendo derivado exclusivamente do `TenantContext`/JWT; nenhum tenant e aceito por parametro.
- A consulta exige `VENDA_PEDIDO_LER`.
- Os totais sao calculados a partir dos itens persistidos: subtotal bruto, desconto total e total liquido.
- A operacao e read-only e nao gera auditoria por nao alterar estado de negocio.
- Nenhuma migration e necessaria.

## Fora deste incremento

Reimpressao fiscal, segunda via de comprovante, cancelamento de venda faturada e alteracao de forma de pagamento permanecem fora deste endpoint e devem respeitar os fluxos fiscal e financeiro especificos.

A TRAXUP Central permanece separada do runtime do TPlug ERP.
