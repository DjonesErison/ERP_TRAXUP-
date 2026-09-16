# Identidade visual na homologação

A interface funcional utiliza a identidade TRAXUP existente no catálogo da Central. Referências originais preservadas em `main`, commit `1311791cb822050af110af9ecf935d1c6e8296b4`:

- `docs/design/00-identidade/traxup-logo.webp`: arquivo original, copiado sem alterações para os assets do frontend.
- `docs/design/01-traxup-erp/UI-016-login-traxup-erp.webp`: composição do login, painel azul, mensagem institucional, formulário e ação laranja.
- `docs/design/01-traxup-erp/UI-001-dashboard-traxup-erp.webp`: navegação azul, conteúdo claro e hierarquia de cartões.

## Aplicação

Login responsivo, marca, nome TRAXUP, navegação dos módulos existentes, cores compartilhadas e indicação de homologação. CRM, vendas, compras, inventário, financeiro e contabilidade conservam suas funções e dados. Inventário passa a ter seleção própria no menu, acessível a partir de qualquer módulo.

## Adaptações necessárias

- O backend exige empresa (tenant), e-mail e senha; o campo Empresa continua obrigatório. Não existe seleção global de empresas antes da autenticação.
- O logo original tem fundo claro; é exibido integralmente em uma superfície clara sobre o painel azul, sem redesenhar a marca.
- O laranja dos botões é mais escuro para manter contraste com o texto branco.
- Recuperação de senha orienta contato com o administrador. Recuperação automática, IA, busca global e demais fluxos desenhados não são anunciados como implementados.
- Métricas e listas vêm das APIs existentes, sem números ilustrativos do protótipo.

## Validação e continuidade

A CI compila a aplicação, executa os testes backend, valida autenticação HTTP e abre o login real no Chrome. `visual-smoke.mjs` também verifica desktop/celular, imagem carregada, senha visível/oculta, erro de credenciais, navegação e logout usando respostas simuladas exclusivamente no navegador. Capturas ficam no artefato `homologacao-visual`. Esses testes visuais não substituem a validação funcional com dados reais do usuário.

Novas telas devem apontar para sua referência UI aprovada e reutilizar as variáveis de `src/styles.css`. A promoção continua feature → PR e testes → homologacao → deploy → validação do usuário. Esta alteração não promove o ERP para `main`, não altera Central/roadmap/Etapa 0 e não inclui a PR #324.
