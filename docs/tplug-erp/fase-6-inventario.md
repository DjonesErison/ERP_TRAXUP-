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

## Bloco 2 — Ajuste explícito e rastreável de estoque

O ajuste de estoque foi separado da conclusão da contagem.

- `POST /api/v1/inventarios/{inventarioId}/ajustar-estoque` exige `INVENTARIO_AJUSTAR`;
- somente inventário `CONCLUIDO` pode ser ajustado;
- o ajuste é idempotente por sessão e não pode ser reaplicado;
- cada diferença real gera movimentação padrão de estoque `AJUSTE` com motivo `INVENTARIO:{inventarioId}`;
- itens cujo saldo atual já coincide com a quantidade contada não geram movimentação sem efeito;
- sessão registra usuário e instante do ajuste e mantém auditoria `AJUSTAR_ESTOQUE`.

## Bloco 3 — Conferência de divergências

Implementada consulta dedicada para a conferência operacional:

- `GET /api/v1/inventarios/{inventarioId}/divergencias` — retorna somente contagens cuja divergência é diferente de zero;
- aceita `limite` com padrão 100 e máximo 500;
- valida a existência da sessão dentro do tenant antes da consulta;
- reutiliza `INVENTARIO_LER` e o índice parcial de divergências já criado na V59;
- não altera saldo, estado, auditoria ou movimentações.

## Segurança e auditoria

- tenant vem exclusivamente do contexto autenticado;
- filial sempre é validada por `tenant + filial`;
- produtos e grades são validados dentro do tenant;
- `INVENTARIO_LER` protege consultas;
- `INVENTARIO_EDITAR` protege criação, contagem, conclusão e cancelamento;
- `INVENTARIO_AJUSTAR` protege o ajuste de estoque;
- mutações geram auditoria `CRIAR`, `CONTAR`, `CONCLUIR`, `CANCELAR` e `AJUSTAR_ESTOQUE`;
- as FKs de sessão, filial e usuário incluem `tenant_id` onde aplicável.

## Regra operacional

**Concluir um inventário não altera o saldo de estoque.** A conclusão fecha a contagem para conferência. O estoque só é alterado por uma chamada explícita ao endpoint de ajuste, que registra movimentações auditáveis.

## Próximos blocos

- leitura por código de barras na interface mobile usando os cadastros existentes;
- enriquecimento da conferência com descrição/código do item, sem duplicar dados no inventário;
- suporte operacional a contagem cega, somente se essa regra for formalmente adotada.

A TRAXUP Central permanece separada e sem alteração de runtime.
