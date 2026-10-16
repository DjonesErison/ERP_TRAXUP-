# UI-023 — Administração de Clientes TRAXUP

## Status visual

- Aprovada em 15/10/2026.
- Referência estrutural: `admin-clientes-saas.webp`.
- Arquivo visual oficial: `UI-023-administracao-clientes-traxup.webp`.

## Finalidade

Administrar empresas originadas no Trial e acompanhar sua evolução para clientes pagantes, incluindo assinatura, módulos, cobrança, suporte e histórico operacional.

## Fluxo de situação

Lead do Trial → Trial ativo → Conta ativada → Cliente ativo → Suspenso ou cancelado.

## Estrutura da tela

- Navegação lateral: Visão Geral, Clientes, Trials, Assinaturas, Financeiro, Planos e módulos, Certificados, Suporte, Notificações, Relatórios e Configurações.
- Indicadores: clientes ativos, em teste, inadimplentes, MRR e cancelamentos no mês.
- Filtros: situação, plano, segmento, vencimento e módulos.
- Tabela de empresas com plano, módulos, vencimento, mensalidade, situação e ações.
- Painel lateral com resumo, faturamento, certificados e logs.
- Blocos inferiores de receita, ações necessárias e últimas atividades.

## Ações previstas para o protótipo funcional

- Buscar, filtrar e paginar clientes.
- Exportar a relação de empresas.
- Abrir detalhes da assinatura.
- Criar um cliente.
- Alterar plano e módulos.
- Gerar segunda via e enviar cobrança.
- Prorrogar Trial.
- Bloquear acesso.
- Acessar a conta como suporte.

## Identidade visual obrigatória

- Usar exclusivamente os ativos aprovados de `docs/design/13-identidade-visual-2.0/`.
- Usar `Logo.png` oficial, sem recriação ou substituição.
- Logomarca aplicada diretamente sobre o fundo azul/gradiente, sem caixa branca.
- Azul como cor principal, verde para sucesso, laranja para chamadas e alertas e vermelho apenas para risco.
- Preservar proporções, espaçamentos, hierarquia e legibilidade da imagem aprovada.

## Orientação para implementação

A implementação deve ser realizada no chat **Implementação e Autenticação Telas**, em Angular e TailwindCSS, com responsividade, interações funcionais e link HTML de validação. Este arquivo não representa implementação de código.
