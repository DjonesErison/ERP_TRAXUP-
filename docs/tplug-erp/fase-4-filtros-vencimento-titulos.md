# Fase 4 - Filtros operacionais de titulos

Este incremento adiciona filtros read-only aos livros de contas a receber e contas a pagar sem introduzir regra de juros, multa, cobranca automatica ou liquidacao externa.

## Contrato

- `GET /api/v1/financeiro/contas-receber` aceita opcionalmente `status`, `vencimentoInicio` e `vencimentoFim`.
- `GET /api/v1/financeiro/contas-pagar` aceita os mesmos filtros.
- Datas usam ISO `yyyy-MM-dd` e o periodo e inclusivo.
- Quando inicio e fim forem informados, o inicio nao pode ser posterior ao fim.
- Status e normalizado para maiusculas antes da consulta.
- Contas a receber aceitam `ABERTO`, `PARCIAL`, `RECEBIDO` e `CANCELADO`.
- Contas a pagar aceitam `ABERTO`, `PARCIAL`, `PAGO` e `CANCELADO`.
- Sem parametros, a listagem continua retornando todos os titulos do tenant, ordenados por vencimento crescente e criacao decrescente.

## Isolamento e seguranca

- Toda consulta inclui obrigatoriamente `tenant_id`; os filtros nunca substituem o escopo do tenant autenticado.
- As permissoes existentes permanecem `FINANCEIRO_RECEBER_LER` e `FINANCEIRO_PAGAR_LER`.
- O incremento e somente leitura e nao gera auditoria de mutacao.
- Nenhuma migration e necessaria.

## Limites intencionais

- Vencimento nao altera automaticamente o status persistido do titulo.
- Juros de mora, multa, protesto, boleto e cobranca automatica permanecem fora deste incremento.
- O cliente pode compor consultas de vencidos usando status aberto/parcial e uma faixa de vencimento adequada, sem o backend inventar uma politica de calendario ou encargos.
- A TRAXUP Central permanece separada do runtime do TPlug ERP.
