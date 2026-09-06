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
- Movimentos ja utilizados em outra conciliacao nao aparecem como candidatos.

## Importacao em lote por API

- A API aceita lotes de ate 500 lancamentos por conta financeira.
- O lote e transacional e reutiliza as mesmas validacoes multi-tenant da importacao individual.
- Repeticoes identicas sao retornadas sem nova persistencia ou auditoria; referencias reutilizadas com conteudo diferente geram conflito.

## Adaptador OFX

- O endpoint OFX converte `STMTTRN` para o contrato generico de importacao em lote.
- `FITID` e a referencia idempotente, `TRNAMT` define `ENTRADA`/`SAIDA`, `DTPOSTED` define a ocorrencia e `MEMO`/`NAME` a descricao.
- O adaptador nao persiste diretamente e nao conhece fornecedor especifico.

## Taxas, antecipacoes, estornos e chargebacks

- Todo lancamento externo inicia com natureza `NORMAL`.
- Enquanto estiver `PENDENTE`, pode ser classificado como `NORMAL`, `TAXA`, `ANTECIPACAO`, `ESTORNO` ou `CHARGEBACK`.
- A classificacao permanece isolada por tenant e usa bloqueio do lancamento para nao competir com a conciliacao concorrente.
- A alteracao exige `FINANCEIRO_CONCILIACAO_EDITAR` e gera auditoria `CLASSIFICAR` em `CONCILIACAO_FINANCEIRA`.
- Lancamentos ja conciliados nao podem ser reclassificados.
- A classificacao nao cria movimento, nao altera saldo e nao presume contabilizacao automatica por adquirente/PSP.
- O PostgreSQL restringe as naturezas validas e possui indice por tenant, natureza e status.

## RBAC e auditoria

- `FINANCEIRO_CONCILIACAO_LER`: consulta lancamentos importados e sugestoes.
- `FINANCEIRO_CONCILIACAO_EDITAR`: importa, importa em lote, importa OFX, classifica e concilia lancamentos.
- Nova importacao gera auditoria `IMPORTAR`.
- Classificacao gera auditoria `CLASSIFICAR`.
- Matching efetivado gera auditoria `CONCILIAR`.

## Proximos incrementos

1. consultas e filtros operacionais por natureza, origem, status e periodo;
2. integracoes bancarias/PSP especificas por adaptadores;
3. regras de contabilizacao/liquidacao especificas quando o provedor exigir.

A TRAXUP Central permanece separada do runtime do TPlug ERP.
