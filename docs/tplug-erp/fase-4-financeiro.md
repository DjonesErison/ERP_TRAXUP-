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
- Baixas e cancelamentos carregam o titulo com lock pessimista de escrita por `titulo + tenant`; operacoes concorrentes para o mesmo titulo sao serializadas e a seguinte reavalia saldo e estado ja atualizados antes de prosseguir.
- Regras financeiras invalidas retornam HTTP 400.
- Baixas integrais anteriores a V27 recebem um movimento historico na V28.
- Criacao, baixa e cancelamento geram auditoria.

### RBAC de recebimentos

- `FINANCEIRO_RECEBER_LER`
- `FINANCEIRO_RECEBER_CRIAR`
- `FINANCEIRO_RECEBER_BAIXAR`
- `FINANCEIRO_RECEBER_CANCELAR`

## Contas a pagar

O nucleo de contas a pagar usa fornecedor ativo do mesmo tenant e filial obrigatoria, com pagamentos integrais ou parciais, lock pessimista nos caminhos de alteracao, auditoria e RBAC `FINANCEIRO_PAGAR_*`.

- Estados: `ABERTO`, `PARCIAL`, `PAGO` e `CANCELADO`.
- Pagamento parcial altera o titulo para `PARCIAL`; pagamentos subsequentes utilizam somente o saldo aberto.
- O valor de cada pagamento deve ser positivo e nunca pode superar o saldo aberto.
- Ao atingir o valor original, o titulo passa para `PAGO` e registra `pago_em`.
- Cada baixa gera registro imutavel em `contas_pagar_pagamentos`, com tenant, filial, titulo, valor, usuario e data/hora.
- O endpoint de pagamento integral permanece compativel e liquida apenas o saldo restante calculado com o titulo ja carregado sob lock de escrita.
- Titulos com qualquer pagamento nao podem ser cancelados; cancelamento permanece permitido apenas em `ABERTO`.
- A consulta do historico valida primeiro `titulo + tenant`, e as FKs de filial, fornecedor, titulo e pagamentos preservam o tenant no PostgreSQL.
- Pagamentos integrais existentes antes da V39 sao retroalimentados no historico durante a migracao.
- Criacao, cada baixa e cancelamento geram auditoria.

### RBAC de pagamentos

- `FINANCEIRO_PAGAR_LER`
- `FINANCEIRO_PAGAR_CRIAR`
- `FINANCEIRO_PAGAR_BAIXAR`
- `FINANCEIRO_PAGAR_CANCELAR`

## Caixa e contas bancarias

O primeiro incremento de tesouraria cria contas financeiras por filial dos tipos `CAIXA` e `BANCO`.

- Cada conta pertence obrigatoriamente a tenant e filial por FK composta.
- Nomes sao unicos por tenant/filial.
- Saldo inicia em zero e e alterado apenas por movimentos `ENTRADA` ou `SAIDA`.
- Cada movimento e imutavel e registra tenant, filial, conta, valor, descricao, usuario e data/hora.
- Saida maior que o saldo e bloqueada; saldo negativo nao e permitido neste incremento.
- Movimentacao e desativacao carregam a conta financeira com lock pessimista de escrita por `conta + tenant`, serializando alteracoes concorrentes de saldo da mesma conta.
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
- Pagamento de conta a pagar gera `SAIDA` pelo mesmo valor informado; quando o valor e omitido, liquida o saldo restante para manter compatibilidade com o fluxo integral.
- Titulo e conta financeira precisam pertencer ao mesmo tenant e a mesma filial.
- O endpoint exige simultaneamente a permissao de baixa do titulo e `FINANCEIRO_CONTA_MOVIMENTAR`.
- Historico do titulo, movimento da conta, saldo e auditorias participam da mesma transacao; qualquer falha reverte o conjunto.
- Saldo insuficiente, conta inativa, estado invalido do titulo ou reavaliacao concorrente que invalide a operacao impedem a baixa financeira completa.
- Os endpoints antigos de baixa sem tesouraria permanecem disponiveis para compatibilidade e fluxos que ainda nao informam conta financeira.

## Origem automatica a partir de vendas

O faturamento de pedido de venda integra estoque e financeiro na mesma transacao.

- Pedido faturado com cliente gera automaticamente contas a receber no mesmo tenant e filial.
- O total liquido dos itens e a base comercial inicial; desconto da condicao e aplicado primeiro e juros de venda, quando configurados, incidem depois do desconto.
- A entrada e calculada sobre o total financeiro ajustado, gera titulo proprio com vencimento na data do faturamento e reduz apenas o saldo a parcelar.
- Somente o saldo remanescente e distribuido pela grade de parcelas; a ultima parcela absorve diferencas de arredondamento.
- Condicoes sem juros nao geram qualquer acrescimo. Taxa de adquirente e juros de mora permanecem dominios separados.
- Entrada de 100% gera apenas o titulo de entrada, sem parcelas futuras.
- Pedido sem cliente continua podendo ser faturado sem titulo automatico, preservando vendas sem identificacao do consumidor.
- Se a criacao financeira falhar, o faturamento inteiro e revertido junto com as movimentacoes de estoque.
- O plano financeiro da previa e o faturamento usam a mesma calculadora deterministica, evitando divergencia entre simulacao e execucao.
- O faturamento adquire lock pessimista de escrita por `pedido + tenant` antes de qualquer baixa de estoque ou geracao financeira; requisicoes concorrentes para o mesmo pedido sao serializadas e a segunda e rejeitada ao reenxergar o pedido como `FATURADO`.
- O reflexo tributario automatico de desconto, juros e entrada ainda nao faz parte deste fluxo; o modulo fiscal permanece responsavel por definir esse tratamento quando implementado.

## Configuracao de formas e condicoes de pagamento

O cadastro financeiro de pagamento e configuravel por tenant, sem acoplamento a adquirente, banco ou PSP especifico.

- Formas de pagamento possuem `codigo`, `nome` e estado ativo/inativo, com codigo unico por tenant.
- Condicoes de pagamento possuem `codigo`, `nome` e uma grade ordenada de parcelas.
- Cada parcela define numero sequencial, quantidade de dias apos o faturamento e percentual do saldo parcelavel.
- A soma dos percentuais deve ser exatamente 100% e a numeracao deve iniciar em 1 sem lacunas.
- Dias negativos e percentuais nulos/negativos sao bloqueados na aplicacao e por constraints do PostgreSQL.
- A condicao pode configurar opcionalmente `juros`, `desconto` e `entrada`, cada um como `PERCENTUAL` ou `VALOR_FIXO`.
- Cada ajuste precisa informar tipo e valor em conjunto e ter valor positivo; desconto e entrada percentuais nao podem superar 100%.
- Esses ajustes sao regras comerciais reutilizaveis da condicao, isoladas por tenant e aplicadas ao plano financeiro do faturamento conforme a ordem `desconto -> juros -> entrada -> parcelas`.
- Override de juros, desconto ou entrada diretamente no pedido permanece fora deste incremento e, se adotado, devera possuir permissao e auditoria especificas.
- Todas as consultas e alteracoes sao isoladas por tenant.
- Criacao e desativacao geram auditoria.
- RBAC: `FINANCEIRO_PAGAMENTO_CONFIG_LER` e `FINANCEIRO_PAGAMENTO_CONFIG_EDITAR`.
- Formas e condicoes inativas permanecem historicamente consultaveis, mas novas configuracoes de venda utilizam apenas configuracoes ativas.

## Parcelamento e previa financeira no pedido de venda

O pedido de venda pode registrar forma e condicao de pagamento enquanto estiver em `RASCUNHO`.

- Forma e condicao sao buscadas sempre pelo tenant do pedido e precisam estar ativas no momento da configuracao.
- As FKs do pedido para forma e condicao sao compostas por `tenant_id`, impedindo referencias cruzadas entre tenants tambem no PostgreSQL.
- A configuracao exige `VENDA_PEDIDO_EDITAR` e gera auditoria `CONFIGURAR_PAGAMENTO`.
- No faturamento, cada parcela prevista gera uma conta a receber independente, com vencimento calculado pela quantidade de dias da parcela.
- O valor de cada parcela deriva do percentual configurado sobre o saldo depois da entrada; a ultima parcela absorve diferencas de arredondamento para que a soma dos titulos seja exatamente o total financeiro ajustado.
- O documento financeiro identifica pedido e numero da parcela; a entrada usa identificacao propria.
- Pedidos antigos ou ainda sem condicao configurada mantem compatibilidade: geram uma unica parcela com vencimento na data do faturamento.
- `GET /api/v1/vendas/pedidos/{pedidoId}/previa-financeira`, protegido por `VENDA_PEDIDO_LER`, retorna total liquido, desconto, juros, entrada, total financeiro, saldo a parcelar e titulos previstos sem gerar estoque, conta a receber ou auditoria.
- A previa e read-only e utiliza o mesmo plano financeiro tenant-scoped do faturamento.
- Faturamento, estoque e todas as parcelas financeiras participam da mesma transacao; qualquer falha reverte o conjunto.

Conta bancaria compartilhada entre filiais e limite/cheque especial continuam fora deste incremento e poderao ser parametrizados sem alterar o ledger basico.

### Proximos blocos planejados

1. o primeiro adaptador bancario/PSP concreto permanece adiado ate a escolha e disponibilidade da documentacao real do provedor; a infraestrutura generica de conciliacao e sincronizacao continua pronta para recebe-lo;
2. o tratamento fiscal de desconto, juros e entrada permanece separado e so deve ser implementado quando as regras tributarias de incidencia e representacao no documento fiscal forem definidas explicitamente;
3. hardenings genericos de operacao, concorrencia, observabilidade e integridade podem continuar sem acoplamento a provedor externo.

A TRAXUP Central permanece separada do runtime do TPlug ERP.
