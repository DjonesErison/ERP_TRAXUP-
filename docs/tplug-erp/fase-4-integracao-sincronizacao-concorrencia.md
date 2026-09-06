# Fase 4 - Concorrencia na sincronizacao financeira

Este hardening protege a etapa transacional de sincronizacao de uma integracao financeira contra duas aplicacoes concorrentes do mesmo estado externo.

## Regra

- A integracao e localizada por `id + tenant` com `PESSIMISTIC_WRITE` antes de importar lancamentos e avancar o checkpoint.
- O lock permanece restrito a etapa atomica de importacao + checkpoint + auditoria.
- A chamada ao adapter externo continua fora da transacao bloqueante, evitando manter lock de PostgreSQL durante I/O de banco, adquirente ou PSP.
- Depois de obter o lock, a sincronizacao reenxerga o estado mais recente da integracao. O `sincronizado_em` continua obrigado a avancar estritamente, de modo que resultado atrasado ou repetido e rejeitado e a transacao inteira e revertida.
- Idempotencia dos lancamentos externos continua garantida por tenant, conta, origem e referencia externa.
- Integracoes inativas continuam rejeitadas antes de qualquer mutacao.

## Isolamento e seguranca

A consulta bloqueante inclui obrigatoriamente o `tenant_id`; nenhum lock ou leitura pode localizar uma integracao de outro tenant. RBAC dos endpoints, auditoria `SINCRONIZAR`, constraints existentes e observabilidade permanecem inalterados.

Este incremento nao adiciona adapter concreto, credencial, regra de liquidacao especifica de provedor nem altera o runtime da TRAXUP Central.
