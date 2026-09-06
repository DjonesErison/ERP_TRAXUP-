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

## Separacoes importantes

- Juros de venda sao opcionais e existem somente quando a condicao de pagamento os configura explicitamente.
- Taxa de adquirente/maquininha nao e juros da venda e permanece no dominio de conciliacao financeira.
- Juros de mora por atraso pertencem ao fluxo de cobranca/contas a receber e nao a este calculo.
- O reflexo tributario de desconto, juros ou entrada permanece fora deste incremento. O modulo fiscal devera aplicar a regra fiscal adequada quando essa etapa for implementada.
- Override diretamente no pedido continua fora do escopo e, se adotado, exigira permissao e auditoria proprias.

A TRAXUP Central permanece separada do runtime do TPlug ERP.
