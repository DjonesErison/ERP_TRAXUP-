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

## Seguranca

- tenant vem exclusivamente do contexto autenticado;
- filtro de filial e validado por `tenant + filial`;
- acesso protegido por `CRM_CLIENTE_RETORNO_LER`;
- a permissao e adicionada ao perfil `ADMIN` existente pela migration `V56`;
- endpoint e somente leitura e nao gera auditoria de mutacao.

## Proximos blocos

- registrar contatos/campanhas de retorno sem armazenar conteudo sensivel desnecessario;
- agenda de follow-up por cliente;
- segmentacoes por frequencia, recencia e valor quando a base comercial exigir;
- notificacoes/campanhas somente apos definir canal e consentimento LGPD aplicavel.

A TRAXUP Central permanece separada e sem alteracao de runtime.
