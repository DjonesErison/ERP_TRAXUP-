# Fase 5 — CRM e retorno de clientes

A Fase 5 inicia o CRM operacional do TPlug ERP reutilizando a base de clientes e o historico real de vendas, sem duplicar cadastros e mantendo isolamento multi-tenant.

## Bloco 1 — Clientes inativos para retorno

Implementado endpoint de leitura para identificar clientes ativos que ja possuem vendas FATURADAS e cuja ultima compra ocorreu ha pelo menos a quantidade de dias informada.

`GET /api/v1/crm/clientes/inativos`

Parametros:

- `filialId` opcional, sempre validada dentro do tenant autenticado;
- `diasInatividade` opcional, padrao 30, permitido de 1 a 3650 dias;
- `limite` opcional, padrao 100, permitido de 1 a 500 registros.

A resposta informa cliente, nome/razao social, nome fantasia, email, telefone, data da ultima compra e quantidade de compras faturadas consideradas. A ordenacao prioriza quem esta ha mais tempo sem comprar.

Somente vendas `FATURADO` entram no calculo; rascunhos, vendas abertas e canceladas nao caracterizam visita concluida. Clientes inativos no cadastro ou que nao estejam marcados como cliente nao sao retornados.

## Bloco 2 — Agenda de follow-up

A agenda transforma a identificacao de clientes em uma acao operacional acompanhavel, sem disparar mensagens por canais externos.

Endpoints:

- `GET /api/v1/crm/followups` — lista follow-ups com filtros opcionais por filial, cliente e status; padrao `PENDENTE` e limite 100, maximo 500;
- `GET /api/v1/crm/followups/{followUpId}` — consulta um follow-up do tenant;
- `POST /api/v1/crm/followups` — cria follow-up para cliente ativo e filial do mesmo tenant;
- `POST /api/v1/crm/followups/{followUpId}/concluir` — conclui follow-up pendente;
- `POST /api/v1/crm/followups/{followUpId}/cancelar` — cancela follow-up pendente.

Estados permitidos: `PENDENTE`, `CONCLUIDO` e `CANCELADO`. Conclusao e cancelamento usam bloqueio pessimista para serializar transicoes concorrentes. O registro guarda assunto, observacao opcional, data agendada e usuarios de criacao/finalizacao.

A auditoria registra apenas identificadores e operacoes (`CRIAR`, `CONCLUIR`, `CANCELAR`); o texto livre da observacao nao e copiado para a trilha de auditoria.

## Seguranca

- tenant vem exclusivamente do contexto autenticado;
- filial e cliente sao validados dentro do tenant;
- leitura protegida por `CRM_CLIENTE_RETORNO_LER`;
- mutacoes protegidas por `CRM_CLIENTE_RETORNO_EDITAR`;
- as permissoes sao versionadas pelas migrations `V56` e `V57` e concedidas ao perfil `ADMIN` existente;
- FKs da agenda incluem `tenant_id` para filial, cliente e usuarios.

## Proximos blocos

- segmentacoes por frequencia, recencia e valor quando a base comercial exigir;
- historico estruturado de interacoes sem armazenar conteudo sensivel desnecessario;
- notificacoes/campanhas somente apos definir canal, consentimento e regras LGPD aplicaveis.

A TRAXUP Central permanece separada e sem alteracao de runtime.
