# Fase 4 - Conciliacao financeira

A conciliacao financeira permanece generica e desacoplada de bancos, adquirentes e PSPs especificos.

## Modelo

- Lancamentos externos sao importados para uma conta financeira existente do mesmo tenant e filial.
- Cada lancamento registra origem, referencia externa, tipo (`ENTRADA` ou `SAIDA`), valor, descricao e data/hora de ocorrencia.
- A chave `(tenant, conta, origem, referencia externa)` torna a ingestao idempotente: repeticoes identicas retornam o registro existente; reutilizar a chave com conteudo diferente gera conflito.
- Importacoes concorrentes para a mesma conta sao serializadas no PostgreSQL.
- O estado inicial e `PENDENTE`; a conciliacao liga o lancamento externo a um movimento interno do ledger.
- Matching exige mesmo tenant, conta, filial, tipo e valor; cada movimento pode ser usado em apenas uma conciliacao por tenant.
- Lancamento e movimento sao bloqueados durante o matching.
- A conciliacao nao altera saldo nem cria movimentos.
- FKs compostas e constraints preservam isolamento e consistencia de estado no PostgreSQL.

## Sugestoes de matching

- Lancamentos `PENDENTE` consultam candidatos da mesma conta, filial, tipo e valor em janela de +/- 3 dias.
- Movimentos ja conciliados nao aparecem como candidatos.
- A sugestao exige `FINANCEIRO_CONCILIACAO_LER`; efetivar o matching exige `FINANCEIRO_CONCILIACAO_EDITAR`.

## Importacao e OFX

- A API aceita lotes de ate 500 lancamentos por conta financeira, em uma unica transacao.
- Repeticoes identicas sao idempotentes; referencias reutilizadas com conteudo diferente geram conflito.
- O adaptador OFX converte `STMTTRN`: `FITID` e referencia externa, `TRNAMT` define `ENTRADA`/`SAIDA`, `DTPOSTED` a ocorrencia e `MEMO`/`NAME` a descricao.
- O adaptador nao persiste diretamente nem conhece fornecedor especifico.

## Taxas, antecipacoes, estornos e chargebacks

- Todo lancamento inicia com natureza `NORMAL`.
- Enquanto `PENDENTE`, pode ser classificado como `NORMAL`, `TAXA`, `ANTECIPACAO`, `ESTORNO` ou `CHARGEBACK`.
- A classificacao e tenant-scoped, bloqueia o lancamento contra concorrencia e gera auditoria `CLASSIFICAR`.
- A classificacao nao cria movimento nem altera saldo.

## Consultas e filtros operacionais

- A listagem por conta aceita filtros opcionais de `origem`, `natureza`, `status`, `inicio` e `fim`.
- Origem, natureza e status sao normalizados para maiusculas antes da consulta.
- O periodo e inclusivo; quando inicio e fim forem informados, inicio nao pode ser posterior ao fim.
- A conta e validada no tenant corrente antes da consulta e o repositorio sempre filtra simultaneamente por `tenant_id` e `conta_financeira_id`.
- A consulta permanece protegida por `FINANCEIRO_CONCILIACAO_LER` e nao gera auditoria de mutacao.

## Base de integracoes bancarias e PSP

- Cada integracao pertence a um tenant, filial e conta financeira existentes e e identificada por um `provedor` normalizado.
- Uma mesma conta pode possuir provedores diferentes, mas nao pode repetir o mesmo provedor.
- A configuracao armazena apenas identificadores operacionais nao secretos; tokens, chaves e certificados nao sao persistidos neste incremento.
- Conta financeira inativa nao aceita nova integracao.
- Criacao e desativacao sao auditadas como `CRIAR` e `DESATIVAR` em `INTEGRACAO_FINANCEIRA`.
- Consulta reutiliza `FINANCEIRO_CONCILIACAO_LER`; configuracao e desativacao reutilizam `FINANCEIRO_CONCILIACAO_EDITAR`.
- A tabela possui FKs compostas por tenant para conta financeira e filial, alem de unicidade por `(tenant, conta, provedor)`.
- Esta base permite adicionar adaptadores concretos sem acoplar credenciais ou regras especificas ao dominio de conciliacao.

## RBAC e auditoria

- `FINANCEIRO_CONCILIACAO_LER`: consulta lancamentos, filtros, sugestoes e configuracoes de integracao.
- `FINANCEIRO_CONCILIACAO_EDITAR`: importa, classifica, concilia e configura integracoes.
- Nova importacao gera auditoria `IMPORTAR`; classificacao gera `CLASSIFICAR`; matching efetivado gera `CONCILIAR`.

## Proximos incrementos

1. primeiro adaptador concreto de banco/PSP sobre a base de integracoes, mantendo segredos fora do banco operacional;
2. sincronizacao incremental e checkpoint por integracao;
3. regras de contabilizacao/liquidacao especificas quando o provedor exigir.

A TRAXUP Central permanece separada do runtime do TPlug ERP.
