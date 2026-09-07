# Fase 4 - Filtro de pessoa nos titulos financeiros

Este incremento adiciona filtros opcionais por contraparte aos titulos financeiros.

- contas a receber aceitam `clienteId`;
- contas a pagar aceitam `fornecedorId`;
- os filtros valem para listagem e resumo e podem ser combinados com `filialId`, `status`, `vencimentoInicio` e `vencimentoFim`.

## Isolamento e seguranca

Toda consulta mantem `tenant_id` como predicado obrigatorio. Quando cliente ou fornecedor e informado, o identificador nunca substitui o tenant e, quando combinado com filial, os tres identificadores precisam corresponder ao mesmo registro. O tenant continua vindo exclusivamente do contexto autenticado.

O RBAC permanece inalterado: `FINANCEIRO_RECEBER_LER` e `FINANCEIRO_PAGAR_LER`. Os endpoints sao somente leitura, portanto nao criam nova auditoria de mutacao.

Nao ha migration, regra fiscal, cobranca automatica, adapter bancario/PSP nem alteracao no runtime da TRAXUP Central.
