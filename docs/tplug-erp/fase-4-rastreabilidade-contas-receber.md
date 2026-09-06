# Fase 4 - Rastreabilidade de contas a receber por origem

Este incremento torna consultavel a identidade de origem adicionada aos titulos automaticos de contas a receber.

## Consulta operacional

`GET /api/v1/financeiro/contas-receber/origens/{origemTipo}/{origemId}` retorna os titulos da origem dentro do tenant corrente e exige `FINANCEIRO_RECEBER_LER`.

- O tipo da origem e normalizado para maiusculas antes da consulta e da criacao automatica.
- A consulta sempre inclui `tenant_id`, impedindo leitura cruzada entre tenants mesmo quando o mesmo `origem_id` existir em clientes diferentes.
- O response de contas a receber passa a expor `origemTipo`, `origemId` e `origemReferencia`.
- Titulos manuais continuam com origem nula.
- Nenhuma mutacao, auditoria adicional ou regra fiscal e introduzida pela consulta.
- A unicidade estrutural criada na V41 continua sendo a garantia de idempotencia para titulos automaticos.

A TRAXUP Central permanece separada do runtime do TPlug ERP.
