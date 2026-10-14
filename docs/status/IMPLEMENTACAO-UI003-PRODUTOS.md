# UI-003 — Produtos

Referência: Prototipo Telas/UI-003-Produtos da main do ERP_TRAXUP-. Implementação HTML/Angular no ERP, sem dados SEED do protótipo.

Endereço: /produtos no domínio da aplicação (homologação: https://apphomologacao.traxup.com.br). Menu Produtos passa a ser link real; acesso direto, recarga e histórico usam a navegação integrada no Dashboard. Não pertence à Central.

## Funcionalidades integradas

- GET /api/v1/produtos: catálogo real do tenant autenticado; não há filtro de filial no contrato desse cadastro.
- Busca por descrição/código/código de barras/grupo, filtros combinados de grupo e situação e ordenação por descrição/mais recentes. Filtragem local sobre a lista devolvida pela API, sem paginação de servidor.
- Indicadores reais de produtos ativos/inativos.
- Tabela com unidade, custo, venda, margem sobre custo (indefinida quando custo zero), status e seleção.
- Painel Geral/Preços/Fiscal/Estoque. Geral/preços/NCM usam dados reais; Estoque declara integração pendente.
- POST /api/v1/produtos: código, descrição, grupo, NCM, preços, código de barras e unidade. Campos obrigatórios, preços não negativos, erro de duplicidade, permissão e servidor. Não grava antes da resposta da API.
- PATCH /api/v1/produtos/{id}/desativar: confirmação, preservação do histórico e atualização após resposta do servidor. Sem exclusão definitiva ou alterações em lote.
- CSV dos registros filtrados ou dos selecionados no filtro atual; aspas, quebras de linha, BOM UTF-8 e neutralização de células que poderiam ser interpretadas como fórmulas. Não inclui dados ocultos por filtro.
- Estados de carregamento/vazio/erro/403/expiração; layout responsivo com rolagem da tabela dentro do painel.

## Dependências

A API atual não oferece atualização/reativação de cadastro, importação, etiquetas, saldo de estoque, combos ou grade neste contrato. Esses recursos do protótipo não são simulados como operações concluídas: edição/importação/etiquetas desabilitadas; estoque/combos mostram indisponibilidade. CFOP/tributação aguardam integração específica. Não coleta campos sem persistência disponível.

## Validação

Build Angular de produção passou (606,75 kB). YAML, node --check e git diff --check. Smoke dedicado com API simulada (sem escrita em ambiente publicado): acesso direto após login, busca/filtros, detalhes/preços/NCM, criação, duplicidade, cancelamento e confirmação de desativação, CSV seguro, recarga, 403 e lista vazia em 320/390/768/1536px. Execução completa no CI do PR pendente.

Merge e publicação dependem de autorização do usuário; Clientes foi adiado conforme decisão atual.
