# Fase 5 — CRM e retorno de clientes

A Fase 5 inicia o CRM operacional do TPlug ERP reutilizando a base de clientes e o historico real de vendas, sem duplicar cadastros e mantendo isolamento multi-tenant.

## Bloco 1 — Clientes inativos para retorno

Implementado endpoint de leitura para identificar clientes ativos que ja possuem vendas FATURADAS e cuja ultima compra ocorreu ha pelo menos a quantidade de dias informada.

`GET /api/v1/crm/clientes/inativos`

Parametros: `filialId` opcional; `diasInatividade` padrao 30 (1..3650); `limite` padrao 100 (1..500).

A resposta informa cliente, contato, data da ultima compra e quantidade de compras faturadas. Somente vendas `FATURADO` entram no calculo.

## Bloco 2 — Agenda de follow-up

A agenda transforma a identificacao de clientes em uma acao operacional acompanhavel, sem disparar mensagens por canais externos.

Endpoints:

- `GET /api/v1/crm/followups`;
- `GET /api/v1/crm/followups/{followUpId}`;
- `POST /api/v1/crm/followups`;
- `POST /api/v1/crm/followups/{followUpId}/concluir`;
- `POST /api/v1/crm/followups/{followUpId}/cancelar`.

Estados: `PENDENTE`, `CONCLUIDO`, `CANCELADO`. Transicoes usam bloqueio pessimista e mutacoes sao auditadas sem copiar observacoes livres.

## Bloco 3 — Metricas RFV

`GET /api/v1/crm/clientes/rfv`

Entrega uma base objetiva para segmentacao comercial usando somente vendas `FATURADO`:

- recencia: ultima compra e dias desde a ultima compra;
- frequencia: quantidade de pedidos faturados distintos;
- valor: soma liquida dos itens faturados e ticket medio.

Filtros opcionais: `filialId`, `inicio`, `fim` e `limite` (padrao 100, maximo 500). Periodos invertidos sao rejeitados antes de consultar os repositorios e a filial, quando informada, deve pertencer ao tenant autenticado.

A ordenacao prioriza maior valor comprado, depois compra mais recente. O endpoint reutiliza `CRM_CLIENTE_RETORNO_LER`, nao persiste classificacoes arbitrarias e nao cria migration nova.

## Seguranca

- tenant vem exclusivamente do contexto autenticado;
- filial e cliente sao validados dentro do tenant;
- leitura protegida por `CRM_CLIENTE_RETORNO_LER`;
- mutacoes protegidas por `CRM_CLIENTE_RETORNO_EDITAR`;
- permissoes versionadas pelas migrations `V56` e `V57`;
- FKs da agenda incluem `tenant_id` para filial, cliente e usuarios.

## Proximos blocos

- classificacao comercial configuravel sobre as metricas RFV, sem thresholds inventados no backend;
- historico estruturado de interacoes;
- notificacoes/campanhas somente apos definir canal, consentimento e regras LGPD aplicaveis.

A TRAXUP Central permanece separada e sem alteracao de runtime.
