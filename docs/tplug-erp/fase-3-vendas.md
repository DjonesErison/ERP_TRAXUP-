# Fase 3 - Vendas

## Fluxo operacional

1. O pedido de venda nasce em `RASCUNHO`.
2. Itens e descontos podem ser alterados somente em `RASCUNHO`.
3. O pedido so pode ser aberto quando possuir ao menos um item do proprio tenant.
4. O pedido `ABERTO` pode ser faturado.
5. O faturamento baixa estoque de forma transacional: produto sem grade movimenta `PRODUTO`; item com grade movimenta `GRADE`.
6. Saldo insuficiente interrompe a transacao e impede o pedido de chegar a `FATURADO`.
7. O pedido faturado nao pode ser faturado novamente nem cancelado pelo fluxo atual.

## Estados do pedido

- `RASCUNHO`: permite manutencao de itens e descontos.
- `ABERTO`: pedido pronto para faturamento.
- `CANCELADO`: fluxo encerrado sem faturamento.
- `FATURADO`: estoque baixado e pedido concluido.

## Seguranca e consistencia

- Tenant sempre derivado do JWT/TenantContext.
- Filial, cliente, produtos e grades sao validados no tenant.
- Cliente pode ser omitido quando a operacao permitir consumidor nao identificado.
- Endpoints usam RBAC com permissoes do modulo de vendas.
- Criacao, abertura, cancelamento, alteracoes de itens/descontos e faturamento possuem trilha de auditoria.
- Faturamento e baixa de estoque ocorrem na mesma transacao de aplicacao.
- O servico central de estoque impede saldo negativo.
- A TRAXUP Central permanece fora do runtime do TPlug ERP.

## Cobertura consolidada

A fase possui testes para transicoes de status, descontos, faturamento, mapeamento produto/grade para estoque e isolamento por tenant.

## Proxima fase

Fase 4: financeiro, iniciando por contas a receber geradas a partir das operacoes comerciais e estrutura base de contas a pagar.
