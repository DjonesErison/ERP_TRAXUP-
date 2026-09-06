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

## Sincronizacao incremental e checkpoint

- Cada integracao pode armazenar um `checkpoint` opaco de ate 500 caracteres e a data/hora da ultima sincronizacao concluida.
- O checkpoint representa apenas a posicao operacional do provedor; ele nao deve conter token, chave, certificado, senha ou qualquer credencial.
- A atualizacao e sempre localizada por `id + tenant`, exige integracao ativa e usa versionamento otimista para impedir perda silenciosa em atualizacoes concorrentes.
- `sincronizado_em` deve avancar estritamente; requisicoes repetidas ou atrasadas retornam conflito e nao podem regredir o checkpoint.
- A resposta da API inclui `versao` para diagnostico e controle operacional de concorrencia.
- Registrar sincronizacao exige `FINANCEIRO_CONCILIACAO_EDITAR` e gera auditoria `SINCRONIZAR` sem copiar o valor bruto do checkpoint para o log.
- Provedores sem cursor podem deixar o checkpoint nulo e utilizar apenas `sincronizado_em` como marcador operacional.

## Importacao e checkpoint atomicos

- O endpoint `/{integracaoId}/sincronizar-lote` recebe os lancamentos ja traduzidos pelo adaptador e o novo checkpoint em uma unica operacao transacional.
- A integracao e sempre localizada por `id + tenant`; integracoes inativas nao podem sincronizar.
- A conta financeira usada na importacao vem da propria integracao, impedindo que o chamador redirecione lancamentos para outra conta.
- A `origem` dos lancamentos e forçada para o `provedor` configurado na integracao, preservando proveniencia e idempotencia sem confiar em valor informado pelo cliente.
- O limite continua sendo 500 lancamentos e todas as validacoes/idempotencia da conciliacao sao reutilizadas.
- O checkpoint so e atualizado depois que todo o lote e importado com sucesso. Qualquer erro de importacao, conflito de referencia, regressao temporal ou falha ao salvar o checkpoint reverte a transacao inteira.
- Cada novo lancamento mantem auditoria `IMPORTAR`; o fechamento bem sucedido do lote gera `SINCRONIZAR` com provedor, horario e quantidade, sem registrar o checkpoint bruto.
- O endpoint exige `FINANCEIRO_CONCILIACAO_EDITAR`.

## SPI de adapters financeiros

- `IntegracaoFinanceiraAdapter` define o contrato interno para um provedor buscar lancamentos e devolver `lancamentos + checkpoint + sincronizadoEm`.
- O adapter recebe apenas a configuracao operacional da integracao; credenciais continuam fora do banco operacional e devem ser resolvidas pela implementacao concreta em mecanismo seguro.
- O registry normaliza o nome do provedor, impede duas implementacoes concorrentes para o mesmo provedor e falha explicitamente quando nao existe adapter registrado.
- O resultado do adapter e limitado a 500 lancamentos e exige data/hora de sincronizacao.
- A orquestracao localiza a integracao por `id + tenant`, bloqueia integracao inativa, resolve o adapter pelo provedor configurado e entrega o resultado ao fluxo atomico de importacao + checkpoint ja existente.
- Nenhum adapter concreto de banco/PSP e inventado neste incremento; o contrato permite adicionar um provedor real sem alterar o dominio de conciliacao.

## Disparo de sincronizacao por adapter

- `POST /api/v1/financeiro/integracoes/{integracaoId}/sincronizar` dispara o adapter registrado para o provedor configurado na integracao.
- O endpoint exige `FINANCEIRO_CONCILIACAO_EDITAR` e nunca recebe tenant, conta, provedor ou checkpoint do cliente; esses dados sao derivados da integracao tenant-scoped e do resultado do adapter.
- Integracao inexistente para o tenant corrente ou inativa e rejeitada antes de qualquer chamada ao adapter.
- O resultado e encaminhado ao mesmo fluxo atomico de importacao + checkpoint, preservando idempotencia, auditoria e rollback integral.
- A resposta reutiliza a visao operacional da sincronizacao, incluindo o estado atualizado da integracao e os lancamentos importados.
- Se nao houver implementacao registrada para o provedor, a chamada falha explicitamente em vez de simular uma integracao externa.

## RBAC e auditoria

- `FINANCEIRO_CONCILIACAO_LER`: consulta lancamentos, filtros, sugestoes e configuracoes de integracao.
- `FINANCEIRO_CONCILIACAO_EDITAR`: importa, classifica, concilia, configura integracoes e executa sincronizacoes.
- Nova importacao gera auditoria `IMPORTAR`; classificacao gera `CLASSIFICAR`; matching efetivado gera `CONCILIAR`; sincronizacao gera `SINCRONIZAR`.

## Proximos incrementos

1. primeiro adapter concreto de banco/PSP implementando a SPI, com credenciais fora do banco operacional;
2. observabilidade operacional de tentativas/falhas de sincronizacao sem registrar segredos;
3. regras de contabilizacao/liquidacao especificas quando o provedor exigir.

A TRAXUP Central permanece separada do runtime do TPlug ERP.
