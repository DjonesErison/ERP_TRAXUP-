# Fase 4 - Resumo de titulos agregado no PostgreSQL

Este incremento otimiza os endpoints de resumo de contas a receber e contas a pagar sem alterar seu contrato funcional.

## Regra

- `GET /api/v1/financeiro/contas-receber/resumo` e `GET /api/v1/financeiro/contas-pagar/resumo` deixam de carregar todos os titulos filtrados para somar em memoria.
- quantidade, valor original, valor liquidado, saldo ativo e contagens por estado passam a ser calculados por agregacoes `COUNT/SUM/CASE` no PostgreSQL.
- os filtros continuam combinando tenant, filial, cliente/fornecedor, status e periodo de vencimento.
- `tenant_id` permanece obrigatorio em toda agregacao; filial e contraparte sao apenas filtros adicionais e nunca substituem o tenant.
- saldo ativo continua considerando somente `ABERTO` e `PARCIAL`; cancelados e liquidados nao compoem esse saldo.
- respostas vazias continuam retornando zeros, preservando o contrato existente.

## Seguranca e arquitetura

- endpoints continuam read-only e preservam `FINANCEIRO_RECEBER_LER` / `FINANCEIRO_PAGAR_LER`;
- nao existe nova mutacao, portanto nao ha nova auditoria de escrita;
- nao ha migration nem mudanca de regra financeira;
- nenhum arquivo de runtime da TRAXUP Central e alterado.
