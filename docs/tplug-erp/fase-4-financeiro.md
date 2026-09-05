# Fase 4 - Financeiro

## Contas a receber

A Fase 4 inicia com um nucleo financeiro desacoplado de bancos, boletos, adquirentes e conciliacao. O livro operacional de titulos a receber possui isolamento multi-tenant, RBAC, auditoria e historico financeiro.

### Regras implementadas

- Toda conta pertence a um tenant e a uma filial.
- O cliente e obrigatorio, precisa existir no mesmo tenant, estar ativo e possuir papel de cliente.
- O valor original deve ser maior que zero.
- Estados: `ABERTO`, `PARCIALMENTE_RECEBIDO`, `RECEBIDO`, `CANCELADO`.
- Cada baixa gera um movimento imutavel com valor, data, observacao e usuario.
- Baixas podem ser parciais ou integrais e nunca podem superar o saldo em aberto.
- A conta e marcada como `RECEBIDO` somente quando o saldo chega a zero.
- O endpoint integral anterior permanece compativel e agora tambem gera movimento.
- Contas com recebimento parcial ou integral nao podem ser canceladas.
- A atualizacao do saldo usa bloqueio pessimista por conta para impedir sobrebaixa concorrente.
- O historico e filtrado simultaneamente por tenant e conta.
- Criacao, cada baixa e cancelamento geram auditoria.
- Baixas integrais anteriores a V27 sao convertidas em movimentos durante a migration.

### API

- `GET /api/v1/financeiro/contas-receber`
- `GET /api/v1/financeiro/contas-receber/{contaId}`
- `POST /api/v1/financeiro/contas-receber`
- `POST /api/v1/financeiro/contas-receber/{contaId}/recebimentos`
- `GET /api/v1/financeiro/contas-receber/{contaId}/movimentos`
- `POST /api/v1/financeiro/contas-receber/{contaId}/receber`
- `POST /api/v1/financeiro/contas-receber/{contaId}/cancelar`

### RBAC

- `FINANCEIRO_RECEBER_LER`
- `FINANCEIRO_RECEBER_CRIAR`
- `FINANCEIRO_RECEBER_BAIXAR`
- `FINANCEIRO_RECEBER_CANCELAR`

### Proximos blocos planejados

1. contas a pagar;
2. caixa e contas bancarias;
3. origem automatica a partir de vendas/faturamento;
4. conciliacao, taxas e integracoes bancarias/PSP.

A TRAXUP Central permanece separada do runtime do TPlug ERP.
