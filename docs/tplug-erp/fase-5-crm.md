# Fase 5 — CRM e retorno de clientes

A Fase 5 inicia o CRM operacional do TPlug ERP reutilizando a base de clientes e o historico real de vendas, sem duplicar cadastros e mantendo isolamento multi-tenant.

## Bloco 1 — Clientes inativos para retorno

`GET /api/v1/crm/clientes/inativos`

Identifica clientes ativos com vendas `FATURADO` cuja ultima compra ultrapassou o periodo informado. Permite filtro por filial, dias de inatividade e limite.

## Bloco 2 — Agenda de follow-up

Endpoints:

- `GET /api/v1/crm/followups`;
- `GET /api/v1/crm/followups/{followUpId}`;
- `POST /api/v1/crm/followups`;
- `POST /api/v1/crm/followups/{followUpId}/concluir`;
- `POST /api/v1/crm/followups/{followUpId}/cancelar`.

Estados: `PENDENTE`, `CONCLUIDO`, `CANCELADO`. Transicoes usam bloqueio pessimista e mutacoes sao auditadas sem copiar observacoes livres.

## Bloco 3 — Metricas RFV

`GET /api/v1/crm/clientes/rfv`

Entrega recencia, frequencia e valor sobre vendas `FATURADO`: ultima compra, dias desde a ultima compra, quantidade de pedidos faturados, valor liquido comprado e ticket medio. Aceita filial, periodo e limite. Nao persiste thresholds comerciais arbitrarios.

## Bloco 4 — Historico estruturado de interacoes

Endpoints:

- `GET /api/v1/crm/interacoes` — lista por filial, cliente, canal, resultado, periodo e limite;
- `GET /api/v1/crm/interacoes/{interacaoId}` — consulta registro do tenant;
- `POST /api/v1/crm/interacoes` — registra uma interacao realizada.

O historico e append-only: nao existem endpoints de edicao ou exclusao. Cada registro guarda filial, cliente, follow-up opcional, canal, resultado, assunto curto, data da interacao e usuario responsavel.

Canais estruturados: `TELEFONE`, `EMAIL`, `WHATSAPP`, `PRESENCIAL`, `OUTRO`.

Resultados estruturados: `CONTATO_REALIZADO`, `SEM_RETORNO`, `INTERESSE`, `SEM_INTERESSE`, `OUTRO`.

Nao existe campo de texto livre extenso. O assunto e limitado a 160 caracteres e a auditoria registra apenas identificadores, canal e resultado. O modulo apenas registra o historico; nao envia mensagens, emails ou WhatsApp.

A migration `V58` cria a tabela com FKs tenant-safe para filial, cliente, follow-up e usuario, alem de indices operacionais por cliente, filial, canal e resultado.

## Seguranca

- tenant vem exclusivamente do contexto autenticado;
- leitura usa `CRM_CLIENTE_RETORNO_LER`;
- registro usa `CRM_CLIENTE_RETORNO_EDITAR`;
- filial, cliente e follow-up sao validados dentro do mesmo tenant;
- follow-up, quando informado, precisa ser do mesmo cliente e filial da interacao;
- nenhuma integracao externa e acionada por estes endpoints.

## Proximos blocos

- classificacao comercial configuravel sobre RFV, sem thresholds inventados no backend;
- campanhas e notificacoes apenas apos definicao explicita de canal, consentimento e regras LGPD;
- painel operacional do CRM no frontend.

A TRAXUP Central permanece separada e sem alteracao de runtime.
