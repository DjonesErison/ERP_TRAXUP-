# UI-002/UI-021/UI-022 — Clientes

Implementação funcional do módulo de Clientes do TRAXUP ERP, baseada nas referências visuais oficiais:
- `docs/design/02-cadastros/clientes.webp`: consulta principal;
- `docs/design/02-cadastros/UI-021-cadastro-cliente-traxup-erp.webp`: cadastro;
- `docs/design/02-cadastros/UI-022-visao-360-cliente-traxup-erp.webp`: visão 360º.

## Endereço
- Homologação: `https://apphomologacao.traxup.com.br/clientes`

## Implementado
- rota própria e navegação real no menu;
- listagem real de pessoas com papel CLIENTE;
- busca por nome, fantasia, CPF/CNPJ, telefone e e-mail;
- filtro de ativos/inativos;
- cadastro de pessoa física ou jurídica;
- indicação cliente/fornecedor;
- visão 360º com dados cadastrais, contatos, endereços e até 50 vendas recentes;
- inclusão real de contato e endereço;
- desativação de cliente;
- estados de carregamento, vazio, conflito, falta de permissão e sessão expirada;
- responsividade desktop/celular.

## Limites honestos
A API atual não possui endpoint de edição cadastral, reativação, importação, limite de crédito, contas a receber, consentimento LGPD, produtos mais comprados ou campanhas. Esses itens da arte não foram simulados. Os valores de compras são identificados como “volume/ticket exibido” porque refletem somente os pedidos retornados pela consulta atual.

## APIs utilizadas
- `GET/POST /api/v1/pessoas`
- `PATCH /api/v1/pessoas/{id}/desativar`
- `GET/POST /api/v1/pessoas/{id}/contatos`
- `GET/POST /api/v1/pessoas/{id}/enderecos`
- `GET /api/v1/vendas/pedidos/recentes?clienteId=...&filialId=...`

Merge e publicação dependem de aprovação do usuário.