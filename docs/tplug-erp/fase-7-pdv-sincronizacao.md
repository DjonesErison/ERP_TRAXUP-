# Fase 7 — Bloco 2: sincronização idempotente do PDV

Primeira entrega do Bloco 2 da Fase 7. Esta etapa estabelece o contrato de confirmação entre uma venda persistida localmente no terminal e a nuvem, sem ainda substituir o modelo comercial de `PedidoVenda`.

## Objetivo

O PDV deve poder registrar uma operação no SQLite local, reenviar a mesma operação após falha de rede e receber a mesma confirmação do servidor sem duplicar a venda lógica.

## Contrato

Endpoint: `POST /api/v1/pdv/sincronizacoes/vendas`.

Campos enviados pelo terminal:

- `terminalId` — terminal previamente cadastrado no Bloco 1;
- `operacaoLocalId` — UUID estável gerado uma única vez no SQLite para a operação;
- `numeroLocal` — sequência local positiva do terminal;
- `checksum` — SHA-256 hexadecimal do conteúdo canônico da operação local;
- `ocorridoEm` — instante em que a operação ocorreu no PDV.

A série não é aceita do cliente. Ela é derivada do terminal cadastrado na nuvem.

## Idempotência

A chave idempotente é `(tenant_id, terminal_id, operacao_local_id)`.

- primeiro envio válido: HTTP `201` e `repetida=false`;
- reenvio com mesmo `operacaoLocalId`, `numeroLocal` e `checksum`: HTTP `200` e a mesma confirmação, com `repetida=true`;
- reenvio da mesma operação com conteúdo diferente: rejeitado;
- reutilização do mesmo número local dentro da série/filial: rejeitada.

## Segurança

- permissão `PDV_SINCRONIZAR`;
- tenant sempre vem do `TenantContext` autenticado;
- terminal precisa pertencer ao tenant e estar ativo;
- filial e série vêm do cadastro tenant-safe do terminal;
- checksum é validado como SHA-256;
- confirmação inicial é auditada sem registrar o payload comercial completo.

## Persistência

A migration `V63` cria `pdv_vendas_sincronizacao` e mantém vínculo composto com filial e terminal. O registro funciona como ACK durável da operação local.

## Próxima fatia do Bloco 2

A próxima entrega deve transportar o payload comercial da venda (itens, totais e pagamentos), convertê-lo de forma idempotente para o núcleo de vendas existente e só então marcar a operação local como totalmente processada. O SQLite continua responsabilidade do aplicativo PDV local; este backend fornece o contrato seguro de sincronização.

A TRAXUP Central permanece sem alteração de runtime.
