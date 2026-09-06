# Fase 4 - Ajustes comerciais no faturamento

Este incremento aplica ao financeiro do faturamento os ajustes opcionais configurados na condicao de pagamento, sem alterar automaticamente documento fiscal ou base tributaria.

## Ordem de calculo

1. O total liquido dos itens continua sendo a base comercial inicial.
2. Desconto configurado e aplicado primeiro.
3. Juros de venda, quando explicitamente configurados na condicao, sao calculados sobre o valor apos desconto. Condicoes sem juros nao geram qualquer acrescimo.
4. A entrada e calculada sobre o total financeiro depois de desconto e juros e nao aumenta o valor da venda.
5. A entrada gera um titulo proprio com vencimento na data do faturamento.
6. Somente o saldo restante e distribuido pela grade de parcelas; a ultima parcela absorve diferencas de arredondamento.
7. Entrada de 100% elimina as parcelas futuras e deixa somente o titulo de entrada.

Percentuais e valores fixos usam a configuracao tenant-scoped da condicao. Desconto fixo maior que o total liquido e entrada maior que o total financeiro sao rejeitados. O faturamento, estoque e geracao dos titulos continuam na mesma transacao.

## Plano financeiro unificado

A composicao dos titulos financeiros e centralizada em uma unica calculadora de plano do pedido. Tanto a previa quanto o faturamento usam exatamente a mesma rotina para aplicar ajustes, gerar a entrada, distribuir o saldo pelas parcelas, calcular vencimentos e absorver arredondamento na ultima parcela.

- A data-base e informada explicitamente ao calculo, evitando regras de vencimento espalhadas em fluxos diferentes.
- Sem condicao configurada, o plano gera uma unica parcela pelo total liquido.
- Com condicao configurada, a busca de condicao e parcelas continua tenant-scoped antes do calculo.
- Se existir saldo a parcelar e a condicao nao possuir grade de parcelas, o plano e rejeitado antes da geracao financeira.
- A unificacao nao altera valores, RBAC, auditoria ou regras fiscais; remove apenas duplicacao de regra entre consulta e execucao.

## Concorrencia do faturamento

O faturamento adquire lock pessimista de escrita sobre o pedido usando `id + tenant_id` antes de validar o estado e iniciar qualquer baixa de estoque ou geracao de contas a receber.

- Duas requisicoes concorrentes para o mesmo pedido sao serializadas pela linha do pedido.
- A segunda requisicao somente prossegue depois da primeira transacao terminar e entao reenxerga o pedido como `FATURADO`, sendo rejeitada antes de movimentar estoque ou gerar novos titulos.
- O lock continua tenant-scoped; um tenant nao consegue bloquear ou faturar pedido pertencente a outro tenant.
- Nao ha nova migration: a protecao utiliza o lock transacional do PostgreSQL/JPA sobre a linha ja existente.

## Previa financeira do pedido

Antes do faturamento, `GET /api/v1/vendas/pedidos/{pedidoId}/previa-financeira` permite consultar como o pedido sera convertido em titulos financeiros.

- Exige `VENDA_PEDIDO_LER` e nao gera qualquer mutacao, auditoria, movimento de estoque ou conta a receber.
- Pedido e condicao de pagamento sao buscados sempre pelo tenant corrente.
- Retorna total liquido, desconto, juros, entrada, total financeiro, saldo a parcelar e os titulos previstos com tipo, numero, vencimento e valor.
- Sem condicao configurada, a previa mostra uma unica parcela no dia corrente, preservando o comportamento de compatibilidade do faturamento.
- Com entrada, a previa apresenta o titulo `ENTRADA` separado e distribui somente o saldo restante pela grade da condicao.
- A mesma calculadora deterministica de plano usada no faturamento e reutilizada na previa, evitando divergencia de regra comercial entre consulta e execucao.

## Separacoes importantes

- Juros de venda sao opcionais e existem somente quando a condicao de pagamento os configura explicitamente.
- Taxa de adquirente/maquininha nao e juros da venda e permanece no dominio de conciliacao financeira.
- Juros de mora por atraso pertencem ao fluxo de cobranca/contas a receber e nao a este calculo.
- O reflexo tributario de desconto, juros ou entrada permanece fora deste incremento. O modulo fiscal devera aplicar a regra fiscal adequada quando essa etapa for implementada.
- Override diretamente no pedido continua fora do escopo e, se adotado, exigira permissao e auditoria proprias.

A TRAXUP Central permanece separada do runtime do TPlug ERP.
