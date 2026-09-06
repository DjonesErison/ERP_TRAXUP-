# Fase 4 - Concorrencia dos writes de integracoes financeiras

## Objetivo

Serializar alteracoes concorrentes sobre a mesma integracao financeira sem acoplar o dominio a banco, adquirente ou PSP concreto.

## Regra

Os fluxos de registro manual de sincronizacao e desativacao passam a adquirir `PESSIMISTIC_WRITE` por `integracao_id + tenant_id` antes de alterar estado.

Isso alinha esses writes ao fluxo de sincronizacao por adapter, que ja usa o mesmo lock na etapa atomica de importacao, checkpoint e auditoria.

## Garantias

- isolamento multi-tenant preservado na propria consulta bloqueante;
- corrida entre sincronizacao e desativacao e serializada;
- dois avancos concorrentes de checkpoint manual reenxergam o estado mais recente antes da validacao monotona;
- leituras permanecem sem lock;
- auditoria e RBAC existentes permanecem inalterados;
- nenhuma migration e necessaria;
- nenhuma chamada externa e executada sob lock;
- runtime da TRAXUP Central permanece separado.

## Testes

Os testes unitarios verificam que `registrarSincronizacao` e `desativar` usam exclusivamente `findByIdAndTenantIdForUpdate` e continuam bloqueando tenant incorreto, integracao inativa e regressao de checkpoint.
