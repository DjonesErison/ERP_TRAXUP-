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

## Vínculo comercial — fatia reiniciada

A migration `V64` adiciona o vínculo tenant-safe entre o ACK do PDV e um `PedidoVenda`.

Endpoint: `POST /api/v1/pdv/sincronizacoes/vendas/rascunho`.

Nesta fatia:

- o ACK continua sendo a identidade estável da operação offline;
- o backend cria no máximo um `PedidoVenda` em estado `RASCUNHO` para cada ACK;
- o número do pedido é gerado no servidor como `PDV-{sincronizacaoId}`, cabendo no limite de 40 caracteres e evitando conflito entre filiais/terminais;
- cliente é opcional e, quando informado, continua validado pelo serviço oficial de vendas;
- filial é derivada do terminal já validado, nunca aceita do payload;
- replay do mesmo ACK devolve o mesmo pedido e não cria outro;
- tentativa de trocar o pedido já vinculado é bloqueada em domínio e por integridade no banco;
- nenhum item, pagamento, estoque, financeiro ou faturamento é processado nesta fatia.

Essa separação é intencional: primeiro provamos o vínculo comercial idempotente; depois adicionamos itens, pagamento e fechamento em entregas independentes.

## Segurança

- permissão `PDV_SINCRONIZAR`;
- tenant sempre vem do `TenantContext` autenticado;
- terminal precisa pertencer ao tenant e estar ativo;
- filial e série vêm do cadastro tenant-safe do terminal;
- checksum é validado como SHA-256;
- criação do ACK e vínculo com pedido são auditados sem registrar payload comercial sensível.

## Persistência

- `V63` cria `pdv_vendas_sincronizacao` como ACK durável da operação local;
- `V64` adiciona `pedido_venda_id` e `pedido_venda_vinculado_em`, com consistência de nulidade, unicidade tenant-safe e FK composta para `pedidos_venda`.

## Próxima fatia do Bloco 2

Adicionar itens do payload comercial ao pedido em `RASCUNHO`, reutilizando `PedidoVendaItemApplicationService`, mantendo replay idempotente e ainda sem faturamento automático. Pagamento e faturamento permanecem fora dessa próxima fatia.

O SQLite continua responsabilidade do aplicativo PDV local. A TRAXUP Central permanece sem alteração de runtime.
