# Fase 4 - Conciliacao financeira

Este incremento inicia a conciliacao financeira de forma generica e desacoplada de bancos, adquirentes e PSPs especificos.

## Modelo

- Lancamentos externos sao importados para uma conta financeira existente do mesmo tenant e filial.
- Cada lancamento registra origem, referencia externa, tipo (`ENTRADA` ou `SAIDA`), valor, descricao e data/hora de ocorrencia.
- A chave `(tenant, conta, origem, referencia externa)` impede importacao duplicada e torna a ingestao idempotente.
- O estado inicial e `PENDENTE`; a conciliacao liga o lancamento externo a um movimento interno do ledger.
- O matching manual exige mesmo tenant, conta, filial, tipo e valor.
- A conciliacao nao altera saldo nem cria movimentos: apenas associa evidencias externas ao ledger existente.
- FKs compostas garantem isolamento multi-tenant tambem no PostgreSQL.

## RBAC e auditoria

- `FINANCEIRO_CONCILIACAO_LER`: consulta lancamentos importados.
- `FINANCEIRO_CONCILIACAO_EDITAR`: importa e concilia lancamentos.
- Importacao gera auditoria `IMPORTAR` em `CONCILIACAO_FINANCEIRA`.
- Matching gera auditoria `CONCILIAR` em `CONCILIACAO_FINANCEIRA`.

## Proximos incrementos

1. sugestao automatica de matching por conta, valor, tipo e janela de datas;
2. importadores/adaptadores de extrato (ex.: OFX/API) sem acoplar o dominio a um fornecedor;
3. taxas, antecipacoes, estornos e chargebacks;
4. integracoes bancarias/PSP especificas por adaptadores.

A TRAXUP Central permanece separada do runtime do TPlug ERP.
