# Fase 4 - Validacao de filtros da conciliacao

Este incremento endurece as consultas operacionais da conciliacao financeira sem adicionar dependencia de banco, adquirente ou PSP.

## Regras

- `origem` continua sendo filtro textual livre e e normalizada com `trim` + maiusculas.
- `natureza`, quando informada, deve ser uma de `NORMAL`, `TAXA`, `ANTECIPACAO`, `ESTORNO` ou `CHARGEBACK`.
- `status`, quando informado, deve ser `PENDENTE` ou `CONCILIADO`.
- `tipo`, quando informado, deve ser `ENTRADA` ou `SAIDA`.
- valores em branco permanecem equivalentes a filtro ausente.
- valores enumerados invalidos geram erro de regra de negocio antes de executar a consulta de lancamentos ou o resumo.
- a validacao vale igualmente para a listagem e para o resumo filtrado.

## Seguranca e arquitetura

- a conta continua sendo validada no tenant corrente antes da consulta;
- os repositorios continuam obrigatoriamente escopados por `tenant_id + conta_financeira_id`;
- RBAC permanece `FINANCEIRO_CONCILIACAO_LER`;
- o fluxo e somente leitura e nao cria novo evento de auditoria;
- nao ha migration nem alteracao de schema;
- a TRAXUP Central permanece separada do runtime do TPlug ERP.
