# Fase 4 - Concorrencia de contas financeiras

Este hardening serializa alteracoes de saldo e estado de contas financeiras sem alterar o contrato publico da tesouraria.

## Regras

- Movimentacoes `ENTRADA` e `SAIDA` adquirem lock pessimista de escrita por `conta + tenant` antes de alterar o saldo.
- A desativacao da conta usa o mesmo lock para evitar corrida com movimentos simultaneos.
- Leituras e listagens permanecem sem lock.
- O filtro de tenant faz parte da consulta bloqueante, preservando isolamento multi-tenant.
- `@Version` permanece como defesa adicional de integridade, mas o fluxo operacional passa a ser serializado antes da mutacao.
- Auditoria, ledger imutavel, RBAC e idempotencia das baixas integradas permanecem inalterados.
- Nao ha alteracao em modulo fiscal, adaptadores externos ou runtime da TRAXUP Central.

## Resultado esperado

Duas operacoes concorrentes sobre a mesma conta financeira passam a enxergar o saldo em ordem deterministica. A segunda operacao somente prossegue depois da primeira transacao liberar o lock, reduzindo conflitos otimistas e impedindo decisoes simultaneas sobre o mesmo saldo.
