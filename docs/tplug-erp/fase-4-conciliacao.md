# Fase 4 - Conciliacao financeira

A conciliacao financeira permanece generica e desacoplada de bancos, adquirentes e PSPs especificos.

## Modelo

- Lancamentos externos sao importados para uma conta financeira existente do mesmo tenant e filial.
- Cada lancamento registra origem, referencia externa, tipo (`ENTRADA` ou `SAIDA`), valor, descricao e data/hora de ocorrencia.
- A chave `(tenant, conta, origem, referencia externa)` impede importacao duplicada e torna a ingestao idempotente.
- O estado inicial e `PENDENTE`; a conciliacao liga o lancamento externo a um movimento interno do ledger.
- O matching manual exige mesmo tenant, conta, filial, tipo e valor.
- A conciliacao nao altera saldo nem cria movimentos: apenas associa evidencias externas ao ledger existente.
- FKs compostas garantem isolamento multi-tenant tambem no PostgreSQL.

## Sugestoes de matching

- Lancamentos `PENDENTE` podem consultar movimentos candidatos sem alterar qualquer estado.
- A busca exige o mesmo tenant, conta financeira, filial, tipo e valor do lancamento externo.
- A janela temporal inicial e de tres dias antes ate tres dias depois da ocorrencia externa.
- O endpoint de sugestoes exige apenas `FINANCEIRO_CONCILIACAO_LER`; efetivar a conciliacao continua exigindo `FINANCEIRO_CONCILIACAO_EDITAR`.
- A sugestao e deterministica e nao faz conciliacao automatica, evitando falsos positivos silenciosos.
- Lancamentos ja conciliados nao recebem novas sugestoes.

## RBAC e auditoria

- `FINANCEIRO_CONCILIACAO_LER`: consulta lancamentos importados e sugestoes.
- `FINANCEIRO_CONCILIACAO_EDITAR`: importa e concilia lancamentos.
- Importacao gera auditoria `IMPORTAR` em `CONCILIACAO_FINANCEIRA`.
- Matching efetivado gera auditoria `CONCILIAR` em `CONCILIACAO_FINANCEIRA`.
- Consultar sugestoes e uma operacao somente leitura e nao gera evento de auditoria de mutacao.

## Proximos incrementos

1. importadores/adaptadores de extrato (ex.: OFX/API) sem acoplar o dominio a um fornecedor;
2. taxas, antecipacoes, estornos e chargebacks;
3. integracoes bancarias/PSP especificas por adaptadores.

A TRAXUP Central permanece separada do runtime do TPlug ERP.
