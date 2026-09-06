# Fase 4 - Conciliacao financeira

A conciliacao financeira permanece generica e desacoplada de bancos, adquirentes e PSPs especificos.

## Modelo

- Lancamentos externos sao importados para uma conta financeira existente do mesmo tenant e filial.
- Cada lancamento registra origem, referencia externa, tipo (`ENTRADA` ou `SAIDA`), valor, descricao e data/hora de ocorrencia.
- A chave `(tenant, conta, origem, referencia externa)` torna a ingestao idempotente: repeticoes identicas retornam o registro existente; reutilizar a chave com conteudo diferente gera conflito.
- Importacoes concorrentes para a mesma conta sao serializadas no PostgreSQL para fechar a janela entre consulta e gravacao.
- O estado inicial e `PENDENTE`; a conciliacao liga o lancamento externo a um movimento interno do ledger.
- O matching manual exige mesmo tenant, conta, filial, tipo e valor.
- Cada movimento do ledger pode ser usado em apenas uma conciliacao por tenant.
- Lancamento e movimento sao bloqueados durante o matching para impedir conciliacoes concorrentes e auditorias duplicadas.
- A conciliacao nao altera saldo nem cria movimentos: apenas associa evidencias externas ao ledger existente.
- FKs compostas garantem no PostgreSQL que tenant, filial e conta do movimento coincidem com o lancamento.
- Constraints de estado exigem `PENDENTE` sem movimento/data de conciliacao ou `CONCILIADO` com ambos preenchidos.

## Sugestoes de matching

- Lancamentos `PENDENTE` podem consultar movimentos candidatos sem alterar qualquer estado.
- A busca exige o mesmo tenant, conta financeira, filial, tipo e valor do lancamento externo.
- A janela temporal inicial e de tres dias antes ate tres dias depois da ocorrencia externa.
- O endpoint de sugestoes exige apenas `FINANCEIRO_CONCILIACAO_LER`; efetivar a conciliacao continua exigindo `FINANCEIRO_CONCILIACAO_EDITAR`.
- A sugestao e deterministica e nao faz conciliacao automatica, evitando falsos positivos silenciosos.
- Movimentos ja utilizados em outra conciliacao nao aparecem como candidatos.
- Lancamentos ja conciliados nao recebem novas sugestoes.

## Importacao em lote por API

- A API aceita lotes de ate 500 lancamentos por conta financeira.
- Todos os itens do lote reutilizam as mesmas validacoes de tenant, filial, conta, origem, referencia externa, tipo e valor da importacao individual.
- O lote e transacional: qualquer item invalido ou referencia reutilizada com conteudo diferente interrompe a operacao e reverte o conjunto.
- Repeticoes identicas sao retornadas sem nova persistencia ou auditoria.
- Cada novo lancamento importado preserva sua propria referencia externa e auditoria `IMPORTAR`.

## Adaptador OFX

- O endpoint OFX converte transacoes `STMTTRN` para o contrato generico de importacao em lote.
- `FITID` e usado como referencia externa idempotente e a origem e registrada como `OFX`.
- `TRNAMT` positivo vira `ENTRADA`; negativo vira `SAIDA`, armazenando o valor absoluto no dominio.
- `DTPOSTED` define a ocorrencia e `MEMO`/`NAME` alimentam a descricao.
- O parser aceita OFX SGML/XML comum sem introduzir dependencia de fornecedor bancario.
- O conteudo recebido e limitado e o lote continua restrito a 500 lancamentos.
- Isolamento de tenant/filial/conta, RBAC e auditoria continuam centralizados no servico de conciliacao; o adaptador apenas traduz formato.

## RBAC e auditoria

- `FINANCEIRO_CONCILIACAO_LER`: consulta lancamentos importados e sugestoes.
- `FINANCEIRO_CONCILIACAO_EDITAR`: importa, importa em lote, importa OFX e concilia lancamentos.
- Nova importacao gera auditoria `IMPORTAR` em `CONCILIACAO_FINANCEIRA`.
- Matching efetivado gera auditoria `CONCILIAR` em `CONCILIACAO_FINANCEIRA`.
- Repeticoes idempotentes e consultas de sugestoes nao geram evento de auditoria de mutacao.

## Proximos incrementos

1. taxas, antecipacoes, estornos e chargebacks;
2. integracoes bancarias/PSP especificas por adaptadores.

A TRAXUP Central permanece separada do runtime do TPlug ERP.
