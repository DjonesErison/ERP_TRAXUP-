# Fase 4 - Limite operacional da listagem de conciliacao

A listagem HTTP de lancamentos de conciliacao passa a ter limite explicito para evitar carregamentos operacionais sem teto.

## Contrato

- `GET /api/v1/financeiro/conciliacao/contas/{contaId}/lancamentos` aceita `limite` opcional.
- O valor padrao e `100`.
- O intervalo permitido e de `1` a `500` registros.
- Valores fora do intervalo sao rejeitados antes da consulta de conciliacao.
- A ordenacao permanece `ocorridoEm DESC, id ASC`.
- Os filtros existentes por origem, natureza, status, tipo e periodo continuam aplicados antes do limite.

## Seguranca e arquitetura

- A conta continua validada no tenant corrente.
- A consulta permanece obrigatoriamente escopada por `tenant_id + conta_financeira_id`.
- O endpoint continua protegido por `FINANCEIRO_CONCILIACAO_LER`.
- A operacao e somente leitura e nao cria auditoria de mutacao.
- As assinaturas internas existentes de listagem foram preservadas para compatibilidade; o novo limite e aplicado pelo contrato HTTP operacional.
- Nao ha migration, adapter concreto, credencial ou alteracao no runtime da TRAXUP Central.
