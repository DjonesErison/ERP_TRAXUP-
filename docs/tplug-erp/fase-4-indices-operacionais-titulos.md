# Fase 4 - Indices operacionais dos titulos financeiros

Este incremento adiciona indices compostos para os filtros operacionais acumulados de contas a receber e contas a pagar.

## Cobertura

- contas a receber: `tenant_id + filial_id + cliente_id + status + vencimento`;
- contas a pagar: `tenant_id + filial_id + fornecedor_id + status + vencimento`.

Os indices complementam os indices simples ja existentes e priorizam o caminho mais seletivo usado quando filial e contraparte sao informadas, sem alterar consultas, contrato de API ou regras financeiras.

## Seguranca e arquitetura

- `tenant_id` permanece como primeira coluna dos indices, preservando o padrao de isolamento multi-tenant;
- nao ha alteracao de RBAC, auditoria ou comportamento funcional;
- a mudanca e exclusivamente de schema/performance e passa pelo Flyway e CI PostgreSQL;
- nenhum arquivo de runtime da TRAXUP Central e alterado.
