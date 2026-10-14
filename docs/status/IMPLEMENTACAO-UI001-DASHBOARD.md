# UI-001 — Dashboard do ERP

Referência aprovada: `Prototipo Telas/UI-001-Dashboard-ERP`, na main do ERP_TRAXUP-. O protótipo contém imagem e hotspots demonstrativos; a tela implementada usa HTML/Angular e API real, sem reproduzir valores fictícios da imagem.

## Endereços

Todos os caminhos pertencem ao mesmo domínio do ERP (homologação: https://apphomologacao.traxup.com.br), sem confundir com central.traxup.com.br.

| Tela | Caminho |
| --- | --- |
| Dashboard | /dashboard |
| Vendas | /vendas |
| Compras | /compras |
| Inventário | /inventario |
| Financeiro | /financeiro |
| Contabilidade | /contabilidade |
| CRM | /crm |
| Configuração inicial | /configuracao |

A raiz continua compatível com o login; após autenticação/onboarding e seleção de filial, o endereço passa a /dashboard. Um link direto a módulo preserva o destino depois do login e da seleção. Menu com links reais permite nova aba. Voltar/Avançar restaura a área. Sessão expirada/logout retorna a /entrar. Módulos ainda não implementados continuam indisponíveis, incluindo Clientes (próxima etapa aprovada).

## Funcionalidades

- Estrutura UI-001: resumo, análises, aplicativos, últimas vendas e pendências; adaptada a celular.
- Consulta real em GET /api/v1/vendas/pedidos/recentes, com filial ativa, número de pedido e período (todos/hoje/7/30 dias). Datas seguem o contrato atual do serviço: limites UTC.
- Até cinco últimas vendas, total de registros da consulta, data/hora, valor e status; sem somar uma página como faturamento global.
- Atualização manual, indicação de horário, estados de carregamento/vazio/403/erro, renovação e expiração da sessão preservados.
- Links Consultar vendas/Ver todas levam a /vendas. Troca de filial utiliza a seleção existente e recarrega dados.

## Dependências pendentes

Faturamento diário, ticket médio, série histórica, distribuição de pagamentos e alertas de estoque/fiscal precisam de APIs agregadas e ainda exibem indisponibilidade. Downloads de PDV/acesso remoto aguardam URLs oficiais; Assistente IA e busca global por clientes/produtos não têm serviço integrado. O campo de busca desta entrega consulta apenas número de pedido.

## Validação

Build Angular de produção e git diff --check. Smoke de navegador ampliado: URLs de menu, login→dashboard, recarga de /vendas, histórico Voltar/Avançar, filtros com filial e limites de período, layout desktop/mobile e falhas/expiração. Execução completa no CI do PR pendente. Merge e publicação dependem de autorização.
