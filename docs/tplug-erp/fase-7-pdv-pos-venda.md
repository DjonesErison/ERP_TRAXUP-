# Fase 7 — Bloco 3: Últimas Vendas e pós-venda do PDV

Esta entrega cria uma fachada operacional própria para o PDV reutilizando o núcleo oficial de vendas já existente. Nenhuma venda paralela é criada.

## Endpoints

Base: `/api/v1/pdv/vendas`.

- `GET /recentes` — lista vendas recentes, com paginação, filtro opcional por filial e status;
- `GET /{pedidoId}` — detalhe da venda, itens, combos e totais;
- `POST /{pedidoId}/segunda-via` — gera uma representação de segunda via a partir da venda persistida e audita a ação;
- `POST /{pedidoId}/cancelar` — delega o cancelamento ao núcleo oficial de vendas;
- `POST /{pedidoId}/pagamento` — delega alteração de forma/condição ao núcleo oficial de vendas.

## Permissões V66

- `PDV_VENDA_LER`;
- `PDV_VENDA_REIMPRIMIR`;
- `PDV_VENDA_CANCELAR`;
- `PDV_VENDA_ALTERAR_PAGAMENTO`.

O perfil ADMIN recebe essas permissões pela migration. Outros perfis devem recebê-las explicitamente conforme política do tenant.

## Regras de segurança

- tenant vem exclusivamente do `TenantContext` autenticado;
- consultas reutilizam os serviços tenant-safe de `PedidoVenda`;
- segunda via só é permitida para venda `FATURADO` e não cria nova operação comercial;
- reimpressão é auditada como `REIMPRIMIR / PEDIDO_VENDA`;
- alteração de pagamento continua limitada pelo domínio a pedidos `RASCUNHO`;
- cancelamento continua limitado pelas regras atuais do domínio; venda `FATURADO` não é cancelada por este fluxo porque ainda exige estorno fiscal/estoque/financeiro próprio;
- nenhuma credencial, token ou dado sensível de cartão é persistido nesta camada.

## Próximos passos

O próximo bloco da Fase 7 é configuração operacional do PDV: regras de cancelamento/justificativa/autorização, fechamento de caixa e parâmetros de impressão. Cancelamento pós-faturamento somente será habilitado quando existir fluxo transacional de estorno e integração fiscal apropriada.

A TRAXUP Central permanece sem alteração de runtime.
