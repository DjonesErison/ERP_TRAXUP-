# Fase 7 — Bloco 2: sincronização idempotente do PDV

O Bloco 2 estabelece o contrato entre a venda persistida localmente no terminal e a nuvem, preservando idempotência e reutilizando o núcleo comercial oficial de `PedidoVenda`.

## Objetivo

O PDV deve poder registrar uma operação no SQLite local, reenviar a mesma operação após falha de rede e receber a mesma confirmação do servidor sem duplicar a venda lógica nem os efeitos de estoque/financeiro.

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

## Processamento comercial

Endpoint: `POST /api/v1/pdv/sincronizacoes/vendas/processar`.

Além dos campos do ACK, o payload transporta:

- cliente opcional;
- forma e condição de pagamento;
- observação opcional;
- itens com produto, grade opcional, quantidade, preço unitário e desconto opcional.

O backend não cria um segundo modelo de venda para o PDV. Ele reutiliza os serviços oficiais de `PedidoVenda` para:

1. criar o pedido com filial derivada do terminal;
2. incluir e validar os itens tenant-safe;
3. aplicar descontos;
4. validar forma e condição de pagamento;
5. abrir o pedido;
6. faturar usando as regras já existentes de estoque, financeiro e auditoria;
7. marcar o ACK como processado somente após o faturamento concluir na mesma transação.

O número do pedido de venda é gerado de forma determinística a partir do identificador da sincronização, mantendo o limite de 40 caracteres e impedindo dependência de numeração enviada pelo terminal.

Se a operação comercial já estiver processada, o replay retorna o mesmo `pedidoVendaId` e não cria novo pedido, nova baixa de estoque ou novo financeiro.

## Segurança

- permissão `PDV_SINCRONIZAR`;
- tenant sempre vem do `TenantContext` autenticado;
- terminal precisa pertencer ao tenant e estar ativo;
- filial e série vêm do cadastro tenant-safe do terminal;
- produto, grade, cliente, forma e condição de pagamento são validados pelos serviços de domínio existentes;
- checksum é validado como SHA-256 e comparado com o fingerprint persistido em reenvios;
- confirmação e processamento são auditados sem registrar payload comercial sensível completo.

## Persistência

- `V63` cria `pdv_vendas_sincronizacao` como ACK durável da operação local;
- `V64` adiciona `pedido_venda_id` e `processado_em`, com vínculo composto tenant-safe ao pedido e constraint que impede estado parcial persistido.

## Próximas fatias do Bloco 2

Ainda ficam para evolução do aplicativo PDV local e do contrato de sincronização:

- armazenamento e fila SQLite no aplicativo desktop;
- política de retentativa/backoff e confirmação local após ACK;
- suporte a múltiplos pagamentos por venda quando o núcleo comercial adotar divisão de pagamentos;
- transporte de dados fiscais específicos somente por contrato com o módulo fiscal isolado.

A TRAXUP Central permanece sem alteração de runtime.
