# Fase 4 - Financeiro

## Contas a receber

A Fase 4 estabelece o nucleo financeiro desacoplado de bancos, boletos, adquirentes e conciliacao. O livro operacional de titulos preserva isolamento multi-tenant, RBAC, auditoria e historico imutavel de recebimentos.

### Regras implementadas

- Toda conta pertence a um tenant e a uma filial, com integridade composta validada no PostgreSQL.
- O cliente e obrigatorio, precisa existir no mesmo tenant, estar ativo e possuir papel de cliente.
- O valor original deve ser maior que zero.
- Estados: `ABERTO`, `PARCIAL`, `RECEBIDO`, `CANCELADO`.
- Uma baixa parcial altera o titulo para `PARCIAL` e reduz o saldo em aberto.
- A soma das baixas nunca pode superar o valor original.
- Ao atingir o valor original, o titulo passa para `RECEBIDO`.
- Cada baixa gera um registro imutavel em `contas_receber_recebimentos`, com tenant, filial, titulo, valor, usuario e data/hora.
- O endpoint de baixa integral permanece compativel e utiliza o saldo restante do titulo.
- Titulos com qualquer recebimento nao podem ser cancelados; cancelamento e permitido somente em `ABERTO`.
- Consulta do historico valida primeiro que o titulo pertence ao tenant corrente.
- Concorrencia usa versao otimista; conflito simultaneo retorna HTTP 409 e reverte saldo e movimento.
- Regras financeiras invalidas retornam HTTP 400.
- Baixas integrais anteriores a V27 recebem um movimento historico na V28.
- Criacao, baixa e cancelamento geram auditoria.

### RBAC de recebimentos

- `FINANCEIRO_RECEBER_LER`
- `FINANCEIRO_RECEBER_CRIAR`
- `FINANCEIRO_RECEBER_BAIXAR`
- `FINANCEIRO_RECEBER_CANCELAR`

## Contas a pagar

O nucleo de contas a pagar usa fornecedor ativo do mesmo tenant, filial obrigatoria, estados `ABERTO`, `PAGO` e `CANCELADO`, baixa integral inicial, concorrencia otimista, auditoria e RBAC `FINANCEIRO_PAGAR_*`.

## Caixa e contas bancarias

O primeiro incremento de tesouraria cria contas financeiras por filial dos tipos `CAIXA` e `BANCO`.

- Cada conta pertence obrigatoriamente a tenant e filial por FK composta.
- Nomes sao unicos por tenant/filial.
- Saldo inicia em zero e e alterado apenas por movimentos `ENTRADA` ou `SAIDA`.
- Cada movimento e imutavel e registra tenant, filial, conta, valor, descricao, usuario e data/hora.
- Saida maior que o saldo e bloqueada; saldo negativo nao e permitido neste incremento.
- Atualizacao de saldo usa versao otimista para impedir perda de atualizacao em movimentos simultaneos.
- Conta inativa permanece consultavel, mas nao aceita novos movimentos.
- Criacao, movimentacao e desativacao geram auditoria.

### RBAC de tesouraria

- `FINANCEIRO_CONTA_LER`
- `FINANCEIRO_CONTA_CRIAR`
- `FINANCEIRO_CONTA_MOVIMENTAR`
- `FINANCEIRO_CONTA_DESATIVAR`

## Baixas integradas a tesouraria

As baixas operacionais podem ser executadas junto com a movimentacao de caixa/banco em uma unica transacao.

- Recebimento de conta a receber gera `ENTRADA` na conta financeira pelo mesmo valor da baixa, inclusive parcial.
- Pagamento de conta a pagar gera `SAIDA` pelo saldo integral do titulo.
- Titulo e conta financeira precisam pertencer ao mesmo tenant e a mesma filial.
- O endpoint exige simultaneamente a permissao de baixa do titulo e `FINANCEIRO_CONTA_MOVIMENTAR`.
- Historico do titulo, movimento da conta, saldo e auditorias participam da mesma transacao; qualquer falha reverte o conjunto.
- Saldo insuficiente, conta inativa, estado invalido do titulo ou conflito otimista impedem a baixa financeira completa.
- Os endpoints antigos de baixa sem tesouraria permanecem disponiveis para compatibilidade e fluxos que ainda nao informam conta financeira.

Conta bancaria compartilhada entre filiais, limite/cheque especial e conciliacao bancaria ficam fora deste incremento e poderao ser parametrizados sem alterar o ledger basico.

### Proximos blocos planejados

1. origem automatica financeira a partir de vendas/faturamento;
2. conciliacao, taxas e integracoes bancarias/PSP;
3. evolucao de pagamentos parciais em contas a pagar.

A TRAXUP Central permanece separada do runtime do TPlug ERP.
