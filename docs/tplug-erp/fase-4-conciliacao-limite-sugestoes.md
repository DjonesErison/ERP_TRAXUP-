# Fase 4 - Limite operacional das sugestoes de conciliacao

O endpoint de sugestoes de matching passa a limitar a quantidade de movimentos retornados por requisicao.

## Regras

- `GET /api/v1/financeiro/conciliacao/lancamentos/{lancamentoId}/sugestoes` aceita o parametro opcional `limite`.
- O limite padrao da API e `20` e o maximo permitido e `100`.
- Valores fora de `1..100` sao rejeitados antes da consulta de movimentos.
- A janela de matching permanece em `+/- 3 dias`.
- O matching continua exigindo mesmo tenant, conta financeira, filial, tipo e valor.
- Movimentos ja utilizados em outra conciliacao do tenant permanecem excluidos.
- A ordenacao continua deterministica por `ocorrido_em ASC, id ASC`.
- O contrato interno anterior sem limite explicito e preservado para compatibilidade; a API publica usa sempre a variante limitada.

## Seguranca e arquitetura

- O lancamento e localizado por `id + tenant`.
- O endpoint permanece protegido por `FINANCEIRO_CONCILIACAO_LER`.
- A operacao e somente leitura e nao gera nova auditoria de mutacao.
- Nao ha migration, adapter de banco/PSP ou credencial nova.
- A TRAXUP Central permanece separada do runtime do TPlug ERP.
