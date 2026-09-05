# Fase 4 - Financeiro

## Contas a receber

A Fase 4 estabelece o nucleo financeiro desacoplado de bancos, boletos, adquirentes e conciliacao. O livro operacional de titulos preserva isolamento multi-tenant, RBAC, auditoria e historico imutavel de recebimentos.

### Regras implementadas

- Toda conta pertence a um tenant e a uma filial.
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
- Criacao, baixa e cancelamento geram auditoria.

### RBAC

- `FINANCEIRO_RECEBER_LER`
- `FINANCEIRO_RECEBER_CRIAR`
- `FINANCEIRO_RECEBER_BAIXAR`
- `FINANCEIRO_RECEBER_CANCELAR`

O historico de recebimentos usa as mesmas permissoes de leitura/baixa do titulo e nao introduz acesso transversal entre tenants.

### Proximos blocos planejados

1. contas a pagar;
2. caixa e contas bancarias;
3. origem automatica a partir de vendas/faturamento;
4. conciliacao, taxas e integracoes bancarias/PSP.

A TRAXUP Central permanece separada do runtime do TPlug ERP.
