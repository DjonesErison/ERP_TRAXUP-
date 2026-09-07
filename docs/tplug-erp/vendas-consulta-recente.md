# Vendas - Ultimas vendas / consulta recente

Este incremento inicia o requisito de consulta de ultimas vendas no TPlug ERP sem acoplar a operacoes fiscais, reimpressao ou cancelamento pos-faturamento.

## Escopo implementado

- `GET /api/v1/vendas/pedidos/recentes?limite=20` retorna os pedidos mais recentes do tenant autenticado.
- O limite padrao e 20, com faixa permitida de 1 a 100 registros.
- A consulta exige `VENDA_PEDIDO_LER` e deriva o tenant exclusivamente do `TenantContext`/JWT.
- A ordenacao e deterministica por `criado_em DESC, id ASC`, inclusive quando dois pedidos compartilham o mesmo instante de criacao.
- A listagem completa de pedidos passa a usar o mesmo desempate deterministico.
- A consulta e read-only e nao gera auditoria, pois nao altera estado de negocio.
- Nenhuma migration e necessaria para este incremento.

## Fora deste incremento

Reimpressao de documento, segunda via de comprovante, cancelamento de venda faturada e alteracao de forma de pagamento continuam separados, pois dependem do documento/fluxo fiscal e das regras de autorizacao que serao definidos nos respectivos modulos.

A TRAXUP Central permanece separada do runtime do TPlug ERP.
