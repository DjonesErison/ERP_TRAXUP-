# Fase 4 - Concorrencia das integracoes financeiras

Este documento consolida o hardening de concorrencia aplicado as integracoes financeiras apos a base de sincronizacao incremental.

## Regra atual

- Toda mutacao de uma integracao financeira e localizada por `id + tenant_id`.
- Sincronizacao atomica, registro manual de checkpoint e desativacao usam a mesma leitura `PESSIMISTIC_WRITE` tenant-scoped.
- O lock protege o agregado antes da decisao de estado, serializando sincronizacao versus sincronizacao, sincronizacao versus desativacao e avancos concorrentes de checkpoint.
- `@Version` permanece como defesa adicional; o lock pessimista e a protecao primaria dos writes concorrentes do agregado.
- `sincronizado_em` continua estritamente monotono. Depois de esperar pelo lock, uma transacao atrasada reenxerga o estado atual e nao pode regredir o checkpoint.
- Importacao dos lancamentos, atualizacao do checkpoint e auditoria `SINCRONIZAR` permanecem na mesma transacao. Falha em qualquer etapa reverte o conjunto.
- A chamada ao adapter externo permanece fora da transacao que segura o lock de PostgreSQL, evitando manter lock de banco durante I/O externo.
- A origem dos lancamentos continua derivada do provedor configurado na integracao, preservando proveniencia e a chave idempotente `(tenant, conta, origem, referencia_externa)`.

## Isolamento e seguranca

- Nenhum lock ou lookup de mutacao remove o filtro de tenant.
- RBAC permanece em `FINANCEIRO_CONCILIACAO_EDITAR` para writes e `FINANCEIRO_CONCILIACAO_LER` para consultas.
- Auditoria existente permanece obrigatoria para sincronizacao e desativacao.
- Credenciais, tokens, chaves e certificados continuam fora do banco operacional.
- Nenhum adapter concreto de banco/PSP e assumido sem documentacao real do provedor.

## Cobertura

Os testes de servico garantem que os caminhos de sincronizacao e writes administrativos usem a leitura bloqueante e nao retornem silenciosamente a leitura comum. O CI do backend continua validando Java 21, PostgreSQL, migrations e build da imagem Docker.

## Limite intencional

O lock protege a aplicacao atomica do resultado externo no banco. A chamada ao provedor nao e executada sob lock. Evitar duas chamadas externas simultaneas em multiplas instancias exigira uma estrategia de lease/distributed execution guard com politica explicita de expiracao e recuperacao; isso nao deve ser inventado antes de existir necessidade operacional concreta.

A TRAXUP Central permanece separada do runtime do TPlug ERP.
