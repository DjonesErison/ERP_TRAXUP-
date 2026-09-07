# Fase 4 - Resumo operacional de titulos

Este incremento adiciona uma visao read-only consolidada para contas a receber e contas a pagar, reutilizando os filtros tenant-scoped de status e vencimento ja existentes.

## Endpoints

- `GET /api/v1/financeiro/contas-receber/resumo`
- `GET /api/v1/financeiro/contas-pagar/resumo`

Ambos aceitam os parametros opcionais `status`, `vencimentoInicio` e `vencimentoFim`, com as mesmas regras da listagem detalhada. O periodo e inclusivo, inicio posterior ao fim e rejeitado e o status e normalizado antes da consulta.

## Conteudo do resumo

A resposta informa:

- quantidade de titulos considerados;
- soma dos valores originais;
- soma do valor efetivamente liquidado por recebimentos ou pagamentos, inclusive baixas parciais;
- `saldoAtivoTotal`, composto somente pelo saldo remanescente dos titulos `ABERTO` e `PARCIAL`;
- quantidades de titulos abertos, parciais, liquidados e cancelados.

Titulos `RECEBIDO`/`PAGO` nao possuem saldo ativo. Titulos `CANCELADO` permanecem no historico e no valor original consolidado quando fizerem parte do filtro, mas nao representam direito ou obrigacao operacional ativa e por isso nao entram em `saldoAtivoTotal`.

## Seguranca e consistencia

- contas a receber exigem `FINANCEIRO_RECEBER_LER`;
- contas a pagar exigem `FINANCEIRO_PAGAR_LER`;
- o tenant vem exclusivamente do contexto autenticado e nunca de parametro do cliente;
- o resumo reutiliza a mesma consulta filtrada tenant-scoped da listagem, evitando ampliar o conjunto visivel ao usuario;
- a operacao e somente leitura e nao gera auditoria de mutacao;
- nenhuma migration foi necessaria;
- juros de mora, multa, boleto, protesto, cobranca automatica e regras fiscais permanecem fora deste incremento.

A TRAXUP Central permanece separada do runtime do TPlug ERP.
