# Fase 4 - Idempotencia das baixas de tesouraria

As baixas integradas de contas a receber e contas a pagar passam a exigir o header `Idempotency-Key` para distinguir uma nova operacao legitima de um retry da mesma requisicao.

## Regras

- A chave e obrigatoria apenas nos endpoints de baixa integrada com caixa/banco; os endpoints antigos de baixa sem tesouraria permanecem inalterados.
- A chave deve ser nao vazia e possuir no maximo 120 caracteres.
- Recebimentos usam origem `RECEBIMENTO_CONTA_RECEBER` e pagamentos usam `PAGAMENTO_CONTA_PAGAR`.
- A identidade estrutural do movimento e `tenant + origem_tipo + origem_id + origem_referencia`.
- O mesmo titulo pode possuir varias baixas parciais legitimas, desde que cada operacao nova utilize uma chave diferente.
- Repetir a mesma chave para o mesmo titulo no mesmo tenant retorna conflito e nao gera nova baixa nem novo movimento.
- A unicidade tambem e garantida no PostgreSQL pela migration V42, cobrindo requisicoes concorrentes que ultrapassem a verificacao previa da aplicacao.
- Em uma corrida, a violacao de unicidade aborta a mesma transacao, revertendo saldo do titulo, historico, saldo da conta financeira, movimento e auditoria.
- Movimentos manuais continuam com origem nula e nao sofrem a restricao de unicidade por origem.
- A listagem de movimentos expoe os campos de origem para rastreabilidade operacional.

O isolamento multi-tenant, RBAC existente e a separacao da TRAXUP Central permanecem inalterados. Nenhuma regra fiscal ou integracao com provedor externo e introduzida neste incremento.
