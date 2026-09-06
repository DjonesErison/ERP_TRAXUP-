# Fase 4 - Idempotencia de origem em contas a receber

Os titulos financeiros gerados automaticamente passam a possuir uma identidade de origem independente do numero do documento.

A chave e composta por `tenant_id`, `origem_tipo`, `origem_id` e `origem_referencia`. Para faturamento de pedidos, `origem_tipo` e `PEDIDO_VENDA`, `origem_id` e o UUID do pedido e a referencia distingue `ENTRADA` de `PARCELA:<numero>`.

O PostgreSQL garante unicidade dessa chave somente quando a origem e informada. Contas a receber criadas manualmente continuam com origem nula e nao sofrem restricao nova de numero de documento.

Essa protecao complementa o lock pessimista do faturamento: o lock serializa requisicoes concorrentes do mesmo pedido e a chave de origem impede duplicidade estrutural de titulos caso outro fluxo tente persistir a mesma origem.

A chave inclui `tenant_id`, portanto a mesma origem logica pode existir em tenants distintos sem colisao. A TRAXUP Central permanece separada do runtime do TPlug ERP.
