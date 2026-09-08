# Fase 4 - Consulta de conciliacao por referencia

Incremento operacional generico para localizar um lancamento externo pela chave de proveniencia ja usada na idempotencia.

- `GET /api/v1/financeiro/conciliacao/contas/{contaId}/lancamentos/referencia` recebe `origem` e `referenciaExterna`.
- `origem` e normalizada com trim e maiusculas; a referencia preserva caixa e recebe trim.
- ambos os parametros sao obrigatorios e valores vazios sao rejeitados antes de acessar repositorios.
- a conta e validada no tenant corrente e a busca usa simultaneamente `tenant_id + conta_financeira_id + origem + referencia_externa`.
- a leitura exige `FINANCEIRO_CONCILIACAO_LER` e nao gera auditoria de mutacao.
- nenhum schema, adapter concreto, credencial ou regra de contabilizacao foi adicionado.
- a TRAXUP Central permanece sem alteracoes de runtime.
