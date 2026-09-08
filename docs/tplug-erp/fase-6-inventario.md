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

Cada contagem aceita `PRODUTO` ou `GRADE`, valida o item dentro do tenant e registra quantidade do sistema, quantidade física, divergência, usuário e instante da contagem.

## Bloco 2 — Ajuste explícito e rastreável de estoque

- `POST /api/v1/inventarios/{inventarioId}/ajustar-estoque` exige `INVENTARIO_AJUSTAR`;
- somente inventário `CONCLUIDO` pode ser ajustado;
- o ajuste é idempotente por sessão;
- diferenças reais geram movimentação `AJUSTE` com origem `INVENTARIO:{inventarioId}`;
- conclusão, por si só, não altera estoque.

## Bloco 3 — Conferência de divergências

- `GET /api/v1/inventarios/{inventarioId}/divergencias` retorna somente divergências;
- aceita limite defensivo;
- resolve código e descrição atuais por tenant sem duplicar cadastro na sessão.

## Bloco 4 — Leitura por código de barras

- `GET /api/v1/inventarios/itens/por-codigo-barras?codigo=...`;
- protegido por `INVENTARIO_LER`;
- consulta restrita ao tenant autenticado;
- prioriza grade única e rejeita códigos ambíguos.

## Bloco 5 — Experiência Angular/mobile

O frontend operacional em `tplug-erp/frontend` oferece abertura e seleção de sessões, leitura, contagem, divergências, conclusão, cancelamento e ajuste explícito, com layout responsivo e tenant vindo exclusivamente do JWT/interceptor.

## Bloco 6 — Captura opcional por câmera

A leitura por câmera funciona como camada opcional sobre o mesmo fluxo de código de barras, usando detecção de capacidade para `getUserMedia` e `BarcodeDetector`. Navegadores sem suporte continuam operando com digitação ou leitor físico. As imagens são processadas localmente e não são enviadas ao backend.

## Bloco 7 — Contagem cega opcional

A contagem cega foi formalmente adotada como opção por sessão:

- `POST /api/v1/inventarios` aceita `contagemCega=true|false`; ausência preserva o modo convencional;
- a opção é persistida na sessão pela migration `V61` e não muda durante a operação;
- enquanto uma sessão cega estiver `ABERTA`, `quantidadeSistema` e `divergencia` são ocultadas das respostas de contagem;
- a consulta de divergências é bloqueada enquanto a sessão cega estiver aberta, evitando vazamento indireto do saldo esperado;
- a quantidade do sistema continua sendo capturada internamente para rastreabilidade;
- após `CONCLUIR`, saldo e divergências ficam disponíveis para conferência antes do ajuste;
- criação registra em auditoria se a sessão foi aberta em modo cego;
- o modo convencional permanece retrocompatível com `contagemCega=false`.

## Segurança e auditoria

- tenant vem exclusivamente do contexto autenticado;
- filial, produtos e grades são validados dentro do tenant;
- `INVENTARIO_LER` protege consultas;
- `INVENTARIO_EDITAR` protege criação, contagem, conclusão e cancelamento;
- `INVENTARIO_AJUSTAR` protege o ajuste de estoque;
- mutações permanecem auditadas.

## Regra operacional

**Concluir um inventário não altera o saldo de estoque.** A conclusão fecha a contagem para conferência. O estoque só é alterado por chamada explícita ao endpoint de ajuste.

A TRAXUP Central permanece separada e sem alteração de runtime.
