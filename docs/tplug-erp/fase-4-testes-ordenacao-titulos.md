# Fase 4 - Testes de ordenacao deterministica dos titulos

Este incremento valida em PostgreSQL a ordenacao deterministica das consultas operacionais de contas a receber e contas a pagar.

Os testes criam titulos com o mesmo vencimento e com empates de data de criacao para confirmar a ordem por vencimento ascendente, criacao descendente e `id` ascendente como criterio final de desempate.

Nao ha alteracao de API, regra financeira, RBAC, auditoria ou migration. Os testes executam em transacao com rollback. A TRAXUP Central permanece separada do runtime do TPlug ERP.
