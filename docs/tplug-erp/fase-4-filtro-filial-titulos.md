# Fase 4 - Filtro de filial nos titulos

Este incremento adiciona `filialId` opcional nas consultas operacionais de contas a receber e contas a pagar, incluindo seus resumos.

## Endpoints afetados

- `GET /api/v1/financeiro/contas-receber`
- `GET /api/v1/financeiro/contas-receber/resumo`
- `GET /api/v1/financeiro/contas-pagar`
- `GET /api/v1/financeiro/contas-pagar/resumo`

O novo parametro pode ser combinado com `status`, `vencimentoInicio` e `vencimentoFim`. Quando omitido, o comportamento permanece igual ao anterior e considera todas as filiais visiveis dentro do tenant corrente.

## Isolamento

- a consulta por filial exige simultaneamente `tenant_id` e `filial_id` no repositorio;
- o tenant continua vindo exclusivamente do contexto autenticado;
- informar uma filial de outro tenant nao amplia acesso, pois nenhum registro satisfaz o par `tenant + filial`;
- o filtro nao muda RBAC: receber continua exigindo `FINANCEIRO_RECEBER_LER` e pagar `FINANCEIRO_PAGAR_LER`;
- listagens e resumos permanecem somente leitura e nao geram auditoria de mutacao.

Nao ha migration, nova regra fiscal, cobranca automatica ou integracao externa neste incremento. A TRAXUP Central permanece separada do runtime do TPlug ERP.
