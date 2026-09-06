# Fase 4 - Hardening de concorrencia nas baixas financeiras

## Objetivo

Eliminar lost update e sobrescrita de saldo/status quando duas operacoes concorrentes atuam sobre a mesma conta a receber ou conta a pagar.

## Implementacao

- `ContaReceberRepository` e `ContaPagarRepository` passam a expor busca tenant-scoped com `PESSIMISTIC_WRITE`.
- Recebimentos, pagamentos e cancelamentos carregam o titulo com lock dentro da mesma transacao antes de alterar saldo/status.
- Consultas de leitura continuam sem lock.
- O filtro por `tenant_id` faz parte da propria query bloqueante, preservando isolamento multi-tenant.
- Auditoria e historicos de recebimento/pagamento permanecem dentro da transacao original.
- RBAC dos controllers nao foi alterado.
- Nenhuma alteracao foi feita no runtime da TRAXUP Central, fiscal ou adaptadores externos.

## Resultado esperado

Duas baixas concorrentes do mesmo titulo sao serializadas no banco. A segunda operacao reavalia o estado mais recente do titulo apos obter o lock e aplica as regras de dominio sobre o saldo atualizado, evitando sobrebaixa e perda silenciosa de atualizacao.

O mesmo mecanismo serializa cancelamento concorrente com baixa, evitando estados finais inconsistentes.
