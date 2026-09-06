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

## Origem automatica a partir de vendas

O faturamento de pedido de venda integra estoque e financeiro na mesma transacao.

- Pedido faturado com cliente gera automaticamente conta a receber no mesmo tenant e filial.
- O valor financeiro usa a soma do total liquido dos itens, portanto descontos ja aplicados sao respeitados.
- Pedido sem cliente continua podendo ser faturado sem titulo automatico, preservando vendas sem identificacao do consumidor.
- Se a criacao financeira falhar, o faturamento inteiro e revertido junto com as movimentacoes de estoque.
- O titulo gerado reutiliza validacoes multi-tenant e auditoria do modulo financeiro.

## Configuracao de formas e condicoes de pagamento

O cadastro financeiro de pagamento e configuravel por tenant, sem acoplamento a adquirente, banco ou PSP especifico.

- Formas de pagamento possuem `codigo`, `nome` e estado ativo/inativo, com codigo unico por tenant.
- Condicoes de pagamento possuem `codigo`, `nome` e uma grade ordenada de parcelas.
- Cada parcela define numero sequencial, quantidade de dias apos o faturamento e percentual do total.
- A soma dos percentuais deve ser exatamente 100% e a numeracao deve iniciar em 1 sem lacunas.
- Dias negativos e percentuais nulos/negativos sao bloqueados na aplicacao e por constraints do PostgreSQL.
- Todas as consultas e alteracoes sao isoladas por tenant.
- Criacao e desativacao geram auditoria.
- RBAC: `FINANCEIRO_PAGAMENTO_CONFIG_LER` e `FINANCEIRO_PAGAMENTO_CONFIG_EDITAR`.
- Formas e condicoes inativas permanecem historicamente consultaveis, mas novas configuracoes de venda utilizam apenas configuracoes ativas.

## Parcelamento financeiro no pedido de venda

O pedido de venda pode registrar forma e condicao de pagamento enquanto estiver em `RASCUNHO`.

- Forma e condicao sao buscadas sempre pelo tenant do pedido e precisam estar ativas no momento da configuracao.
- As FKs do pedido para forma e condicao sao compostas por `tenant_id`, impedindo referencias cruzadas entre tenants tambem no PostgreSQL.
- A configuracao exige `VENDA_PEDIDO_EDITAR` e gera auditoria `CONFIGURAR_PAGAMENTO`.
- No faturamento, cada parcela da condicao gera uma conta a receber independente, com vencimento calculado pela quantidade de dias da parcela.
- O valor de cada parcela deriva do percentual configurado; a ultima parcela absorve diferencas de arredondamento para que a soma dos titulos seja exatamente o total liquido da venda.
- O documento financeiro identifica pedido e numero da parcela.
- Pedidos antigos ou ainda sem condicao configurada mantem compatibilidade: geram uma unica parcela com vencimento na data do faturamento.
- Faturamento, estoque e todas as parcelas financeiras participam da mesma transacao; qualquer falha reverte o conjunto.

Conta bancaria compartilhada entre filiais, limite/cheque especial e conciliacao bancaria ficam fora deste incremento e poderao ser parametrizados sem alterar o ledger basico.

### Proximos blocos planejados

1. conciliacao, taxas e integracoes bancarias/PSP;
2. evolucao de pagamentos parciais em contas a pagar;
3. ampliar condicoes comerciais com juros, desconto e entrada quando o modelo fiscal/financeiro exigir.

A TRAXUP Central permanece separada do runtime do TPlug ERP.
