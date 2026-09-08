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

Cada contagem aceita `PRODUTO` ou `GRADE`, valida o item dentro do tenant e registra quantidade do sistema, quantidade física, divergência, usuário e instante da contagem. A sessão é bloqueada pessimisticamente durante contagens e transições de estado, serializando atualizações concorrentes.

## Bloco 2 — Ajuste controlado de estoque

Inventários concluídos podem aplicar o ajuste explicitamente por permissão própria. O ajuste é idempotente, registra rastreabilidade na sessão e também gera movimentações padrão de estoque do tipo `AJUSTE`, com referência `INVENTARIO:{id}`. Itens que já estiverem conciliados não geram movimentação sem efeito.

## Bloco 3 — Leitura por código de barras

A interface mobile pode resolver produtos e grades ativos pelo código de barras cadastrado:

- `GET /api/v1/inventarios/itens/por-codigo-barras?codigo=...`;
- protegido por `INVENTARIO_LER`;
- consulta sempre restrita ao tenant autenticado;
- retorna `tipoItem`, `itemId`, código, descrição e código de barras;
- quando o mesmo código estiver cadastrado em produto e grade, a grade é priorizada por representar o item de estoque mais específico.

Essa consulta não cria contagem nem altera estoque; ela somente resolve o item para que o fluxo mobile possa reutilizar o endpoint de contagem existente.

## Segurança e auditoria

- tenant vem exclusivamente do contexto autenticado;
- filial sempre é validada por `tenant + filial`;
- produtos e grades são validados dentro do tenant;
- `INVENTARIO_LER` protege consultas;
- `INVENTARIO_EDITAR` protege criação, contagem, conclusão e cancelamento;
- `INVENTARIO_AJUSTAR` protege aplicação de ajuste físico;
- mutações permanecem auditáveis e FKs tenant-scoped preservam isolamento no banco.

## Próximos blocos

- consulta otimizada de divergências e conferência;
- experiência Angular/mobile para leitura e contagem rápida;
- suporte operacional a contagem cega, somente se essa regra for formalmente adotada.

A TRAXUP Central permanece separada e sem alteração de runtime.
