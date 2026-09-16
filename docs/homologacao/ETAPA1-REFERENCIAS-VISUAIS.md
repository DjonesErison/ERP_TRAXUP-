# Etapa 1 — revisão visual do ERP

Status: **97% — aguardando homologação visual e funcional pelo usuário**.

## Base e escopo

Base funcional: `homologacao` em `29efc0a`, que já contém as correções de
identidade, responsividade, autenticação e inclusão de itens de compras.
`develop` em `98d3cfb` não contém todos os ajustes de homologação. A correção
parte da base funcional para não reintroduzir regressões. Não altera a Central
Angular da raiz, a API, as migrations, os tokens nem o interceptor de autenticação.

Fontes consultadas em `main`, commit `fa74ee3`:

- [UI-016 — Login](https://github.com/DjonesErison/ERP_TRAXUP-/blob/fa74ee3/docs/design/01-traxup-erp/UI-016-login-traxup-erp.webp), visual aprovado.
- [UI-001 — Dashboard](https://github.com/DjonesErison/ERP_TRAXUP-/blob/fa74ee3/docs/design/01-traxup-erp/UI-001-dashboard-traxup-erp.webp), direção visual aprovada.
- [UI-018 — Empresa/filial](https://github.com/DjonesErison/ERP_TRAXUP-/blob/fa74ee3/docs/design/01-traxup-erp/UI-018-selecao-empresa-filial-traxup-erp.webp), visual aprovado.
- [UI-019 — Onboarding](https://github.com/DjonesErison/ERP_TRAXUP-/blob/fa74ee3/docs/design/01-traxup-erp/UI-019-onboarding-traxup-erp.webp), visual aprovado.
- `docs/design/DECISOES-VISUAIS-CONSOLIDADAS.md` e catálogo oficial no mesmo commit.

## Implementação e diferenças funcionais explícitas

| Referência | Resultado / limite |
| --- | --- |
| UI-016 | Composição azul/área clara, logo sem cartão branco, quatro benefícios, formulário, ícones vetoriais, ação laranja e versão mobile. Empresa permanece obrigatória: `POST /api/v1/auth/login` exige `tenantId`, e-mail e senha. O checkbox lembra apenas empresa/e-mail; não modifica persistência dos tokens e nunca salva senha. Recuperação de senha abre orientação real ao administrador; não simula envio. |
| UI-001 | Visão Geral passa a ser a home do ERP após login/restauração da sessão. Menu, cartões, painéis, central de aplicativos, últimas vendas e alertas seguem a composição visual. Pedidos e últimas vendas vêm de `/api/v1/vendas/pedidos/recentes`, com permissão aplicada pela API, estados vazio/erro/carregando e renovação de sessão pelo interceptor existente. O total de pedidos é explicitamente total registrado, não apenas hoje. |
| UI-018 | Auditada: não existe no frontend integrado. Há cadastros de empresas/filiais e API administrativa de concessões RBAC, mas não existe fluxo frontend de escolha operacional integrado aos módulos. A home não anuncia uma filial selecionada nem oferece um seletor fictício. Implementação depende de definir/aplicar o contexto de acesso em todos os módulos e conferir permissões; permanece pendente. |
| UI-019 | Auditada: não existe no frontend integrado nem há contrato de progresso/conclusão do onboarding. A API possui cadastros e configurações parciais, mas ainda não há jornada integrada. Não é inserida uma tela que marque configurações como concluídas sem persistência. Permanece pendente. |

Os números, pessoas, lojas, gráficos e alertas ilustrados nas imagens aprovadas
não são dados do ERP. Os agregados diários, ticket, pagamentos, estoque baixo,
alertas fiscais, downloads, IA e demais módulos ainda sem interface são
identificados como indisponíveis/em preparação. Nenhum botão de download leva
a um endereço inventado. Pesquisa global e identidade pessoal são omitidas
até haver fontes/fluxos correspondentes. Essas diferenças devem ser avaliadas
no aceite; este PR não declara toda a UI-001 funcionalmente concluída.

## Validação

- Build de produção do frontend Angular.
- Smoke em navegador desktop e celular: logo carregado, formulário, senha
  visível/oculta, rejeição de credenciais, home após login, dados de vendas,
  vazio/erro/403, renovação/expiração da sessão e navegação pelos módulos existentes.
- Dados autenticados de smoke são fixtures interceptadas apenas no navegador;
  não comprovam autenticação real no ambiente publicado.
- CI de homologação executa testes Java, migrations PostgreSQL, build Docker,
  smoke da aplicação e evidências de navegador antes de permitir publicação.
- Pós-deploy: conferir SHA em `/build-info.json`, disponibilidade da página/API
  e screenshots do login publicado. Login real e aceite visual/funcional final
  pertencem à validação do usuário. **Não elevar a Etapa 1 a 100% automaticamente.**
