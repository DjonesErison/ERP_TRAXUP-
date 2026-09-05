# Fase 4 - Financeiro

## PR inicial: Contas a receber

A Fase 4 inicia com um nucleo financeiro desacoplado de bancos, boletos, adquirentes e conciliacao. O objetivo deste primeiro bloco e estabelecer o livro operacional de titulos a receber com isolamento multi-tenant, RBAC e auditoria.

### Regras implementadas

- Toda conta pertence a um tenant e a uma filial.
- O cliente e obrigatorio, precisa existir no mesmo tenant, estar ativo e possuir papel de cliente.
- O valor original deve ser maior que zero.
- Estados iniciais: `ABERTO`, `RECEBIDO`, `CANCELADO`.
- Neste primeiro incremento, a baixa e integral. Baixas parciais serao tratadas em PR posterior, evitando assumir uma regra contabil/financeira sem o modelo de movimentos.
- Conta recebida nao pode ser recebida novamente nem cancelada.
- Conta cancelada nao pode ser baixada.
- Criacao, baixa e cancelamento geram auditoria.

### RBAC

- `FINANCEIRO_RECEBER_LER`
- `FINANCEIRO_RECEBER_CRIAR`
- `FINANCEIRO_RECEBER_BAIXAR`
- `FINANCEIRO_RECEBER_CANCELAR`

### Proximos blocos planejados

1. movimentos/baixas parciais e historico de recebimentos;
2. contas a pagar;
3. caixa e contas bancarias;
4. origem automatica a partir de vendas/faturamento;
5. conciliacao, taxas e integracoes bancarias/PSP.

A TRAXUP Central permanece separada do runtime do TPlug ERP.
