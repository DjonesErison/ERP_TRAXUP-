# Fase 7 — Bloco 2: sincronização idempotente do PDV

Esta etapa estabelece o contrato de confirmação entre uma venda persistida localmente no terminal e a nuvem, sem duplicar a operação lógica.

## Objetivo

O PDV deve poder registrar uma operação no SQLite local, reenviar a mesma operação após falha de rede e receber a mesma confirmação do servidor sem duplicar a venda lógica.

## Contrato de ACK

Endpoint: `POST /api/v1/pdv/sincronizacoes/vendas`.

Campos enviados pelo terminal:

- `terminalId` — terminal previamente cadastrado no Bloco 1;
- `operacaoLocalId` — UUID estável gerado uma única vez no SQLite para a operação;
- `numeroLocal` — sequência local positiva do terminal;
- `checksum` — SHA-256 hexadecimal do conteúdo canônico da operação local;
- `ocorridoEm` — instante em que a operação ocorreu no PDV.

A série não é aceita do cliente. Ela é derivada do terminal cadastrado na nuvem.

## Idempotência do ACK

A chave idempotente é `(tenant_id, terminal_id, operacao_local_id)`.

- primeiro envio válido: HTTP `201` e `repetida=false`;
- reenvio com mesmo `operacaoLocalId`, `numeroLocal` e `checksum`: HTTP `200` e a mesma confirmação, com `repetida=true`;
- reenvio da mesma operação com conteúdo diferente: rejeitado;
- reutilização do mesmo número local dentro da série/filial: rejeitada.

## Vínculo comercial — V64

A migration `V64` adiciona o vínculo tenant-safe entre o ACK do PDV e um `PedidoVenda`.

Endpoint: `POST /api/v1/pdv/sincronizacoes/vendas/rascunho`.

- o ACK continua sendo a identidade estável da operação offline;
- o backend cria no máximo um `PedidoVenda` em estado `RASCUNHO` para cada ACK;
- o número do pedido é gerado no servidor como `PDV-{sincronizacaoId}`;
- cliente é opcional e, quando informado, continua validado pelo serviço oficial de vendas;
- filial é derivada do terminal já validado, nunca aceita do payload;
- replay do mesmo ACK devolve o mesmo pedido e não cria outro;
- tentativa de trocar o pedido já vinculado é bloqueada em domínio e por integridade no banco.

## Itens comerciais offline — V65

Endpoint: `POST /api/v1/pdv/sincronizacoes/vendas/{sincronizacaoId}/itens`.

Cada item criado localmente recebe um `itemLocalId` UUID estável. A identidade persistida no servidor é `(tenant_id, pdv_sincronizacao_id, pdv_item_local_id)`.

- o ACK precisa pertencer ao tenant autenticado e já possuir `PedidoVenda` vinculado;
- o pedido vinculado permanece obrigatoriamente em `RASCUNHO` para aceitar itens;
- produto, grade, quantidade, preço e vigência de combo continuam validados pelo `PedidoVendaItemApplicationService` oficial;
- primeiro envio cria o item no pedido;
- replay com a mesma identidade e mesmo conteúdo devolve o item existente, sem duplicação;
- replay com produto, grade, quantidade, preço ou pedido divergente é rejeitado;
- a V65 exige que sincronização e item pertençam ao mesmo tenant também no banco, usando FK composta `(tenant_id, pdv_sincronizacao_id)`.

## Pagamento e fechamento controlado

Endpoint: `POST /api/v1/pdv/sincronizacoes/vendas/{sincronizacaoId}/fechamento`.

Payload:

- `formaPagamentoId`;
- `condicaoPagamentoId`.

O endpoint não cria um motor paralelo de pagamento. Ele reutiliza o núcleo oficial de `PedidoVenda`:

1. obtém o pedido vinculado ao ACK dentro do tenant autenticado;
2. adquire lock pessimista do pedido para serializar fechamentos concorrentes;
3. em `RASCUNHO`, valida e configura forma/condição de pagamento;
4. abre o pedido, exigindo que existam itens;
5. fatura pelo fluxo oficial, preservando as regras existentes de estoque, combos e financeiro;
6. toda a sequência ocorre na mesma transação.

Idempotência do fechamento:

- primeiro fechamento válido retorna `201` e `repetida=false`;
- replay de um pedido já `FATURADO` com a mesma forma e condição retorna `200`, `repetida=true` e não refaz estoque/financeiro;
- replay com forma ou condição diferente é rejeitado como conteúdo divergente;
- pedido `CANCELADO` não pode ser faturado;
- tenant diferente recebe recurso não encontrado, sem exposição cruzada.

O fluxo não armazena credenciais, tokens, dados de cartão ou segredos de adquirente. A etapa trabalha somente com os identificadores internos de forma e condição de pagamento já cadastrados no ERP.

## Segurança

- permissão `PDV_SINCRONIZAR`;
- tenant sempre vem do `TenantContext` autenticado;
- terminal precisa pertencer ao tenant e estar ativo;
- filial e série vêm do cadastro tenant-safe do terminal;
- checksum é validado como SHA-256;
- criação do ACK e vínculo com pedido são auditados sem registrar payload comercial sensível;
- itens reutilizam validações e auditoria do núcleo de vendas;
- configuração de pagamento, abertura e faturamento reutilizam as auditorias do `PedidoVendaApplicationService`.

## Persistência

- `V63` cria `pdv_vendas_sincronizacao` como ACK durável da operação local;
- `V64` adiciona `pedido_venda_id` e `pedido_venda_vinculado_em`, com consistência de nulidade, unicidade tenant-safe e FK composta para `pedidos_venda`;
- `V65` adiciona a identidade local idempotente dos itens do PDV e reforça o vínculo tenant-safe entre item e ACK;
- o fechamento não exige nova migration porque estado e referências de pagamento já pertencem ao núcleo de `PedidoVenda`.

## Próxima fatia do Bloco 2

Concluir o contrato operacional do aplicativo PDV local: estado de fila no SQLite, política de retry/backoff e confirmação local somente após ACK/fechamento confirmado pela nuvem. Depois disso, o roadmap avança para o Bloco 3 — Últimas Vendas e pós-venda.

O SQLite continua responsabilidade do aplicativo PDV local. A TRAXUP Central permanece sem alteração de runtime.
