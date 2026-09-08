# Fase 6 — Inventário e contagem mobile

A Fase 6 inicia a base operacional para inventário via celular, reaproveitando o estoque por filial já existente e mantendo o fluxo preparado para uma interface mobile sem acoplar o backend a um dispositivo específico.

## Bloco 1 — Sessões e contagens

Implementado fluxo de inventário com sessões tenant-safe e estados `ABERTO`, `CONCLUIDO` e `CANCELADO`.

Endpoints:

- `GET /api/v1/inventarios` — lista sessões com filtros opcionais por filial e status;
- `GET /api/v1/inventarios/{inventarioId}` — consulta sessão;
- `POST /api/v1/inventarios` — abre inventário para uma filial;
- `GET /api/v1/inventarios/{inventarioId}/contagens` — lista contagens da sessão;
- `POST /api/v1/inventarios/{inventarioId}/contagens` — registra ou reconta um item;
- `POST /api/v1/inventarios/{inventarioId}/concluir` — conclui sessão com ao menos uma contagem;
- `POST /api/v1/inventarios/{inventarioId}/cancelar` — cancela sessão aberta.

Cada contagem aceita `PRODUTO` ou `GRADE`, valida o item dentro do tenant e registra:

- quantidade atual do sistema no momento da contagem/recontagem;
- quantidade física informada;
- divergência = quantidade contada - quantidade do sistema;
- usuário e instante da contagem.

A sessão é bloqueada pessimisticamente durante contagens e transições de estado, serializando atualizações concorrentes do mesmo inventário. Existe somente uma contagem por sessão + tipo + item; uma nova leitura do mesmo item atualiza a contagem anterior.

## Segurança e auditoria

- tenant vem exclusivamente do contexto autenticado;
- filial sempre é validada por `tenant + filial`;
- produtos e grades são validados dentro do tenant;
- `INVENTARIO_LER` protege consultas;
- `INVENTARIO_EDITAR` protege criação, contagem, conclusão e cancelamento;
- mutações geram auditoria `CRIAR`, `CONTAR`, `CONCLUIR` e `CANCELAR`;
- as FKs de sessão, filial e usuário incluem `tenant_id` onde aplicável.

## Regra importante desta etapa

**Concluir um inventário não altera o saldo de estoque.** A contagem e a divergência ficam registradas para conferência. O ajuste físico/contábil do estoque será um bloco separado, com movimentação explícita e auditável, evitando que uma simples conclusão de contagem altere estoque automaticamente.

## Próximos blocos

- consulta otimizada de divergências e conferência;
- aplicação controlada de ajustes por inventário concluído;
- leitura por código de barras na interface mobile usando os cadastros existentes;
- suporte operacional a contagem cega, somente se essa regra for formalmente adotada.

A TRAXUP Central permanece separada e sem alteração de runtime.
