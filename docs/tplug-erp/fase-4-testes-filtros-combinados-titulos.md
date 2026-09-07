# Fase 4 - Testes de filtros combinados dos titulos

Este incremento amplia a cobertura de integracao PostgreSQL das consultas operacionais de contas a receber e contas a pagar.

Os testes validam a composicao simultanea de tenant, filial, cliente/fornecedor, status e periodo de vencimento, incluindo os limites inicial e final como datas inclusivas. Tambem verificam que os resumos agregados vazios retornam zeros em todos os campos.

Nao ha alteracao de API, regra financeira, RBAC, auditoria ou migration. Os testes executam em transacao e fazem rollback ao final. A TRAXUP Central permanece separada do runtime do TPlug ERP.
