# TRAXUP — Decisões Visuais Consolidadas

Este documento preserva as decisões funcionais e visuais já definidas para orientar a implementação do ERP TRAXUP. O catálogo `docs/design/README.md` continua sendo o índice oficial das imagens e de seu nível formal de aprovação.

## Princípios

- As telas pertencem ao ERP TRAXUP; implementação Angular, backend, integração, testes e publicação são tratados no fluxo de desenvolvimento do projeto.
- Aprovação visual não equivale a funcionalidade implementada.
- Não confundir a Central TRAXUP com projetos paralelos.
- Identidade: azul royal para estrutura/navegação, laranja para ação principal, branco/cinza frio para superfícies e verde/amarelo/vermelho para estados.

## Decisões preservadas

### Acesso e onboarding

- Login administrativo: layout simples, fundo azul, logo e área de login com texto “Acesso ao ERP”.
- O login do ERP não deve exibir chamada de “teste 7 dias”; o trial pertence ao fluxo de prospecção/captação.
- Seleção de empresa/filial deve priorizar seleção direta por caixas clicáveis ou listbox, evitando interação desnecessária.
- Recuperação de senha, onboarding e usuários/permissões possuem referências formais catalogadas.

### Navegação e home

- Menu lateral deve contemplar, entre os módulos principais, BI, FISCAL e CRM.
- Home deve oferecer atalhos para chat com IA, download do PDV e download da ferramenta de acesso remoto/suporte.
- Módulos centrais: Clientes, Produtos, Estoque, Vendas, Financeiro, Relatórios e Configurações, além de Fiscal, CRM e BI.

### Clientes

- Cadastro/edição e Visão 360º possuem referências formais catalogadas como UI-021 e UI-022.
- O desenho deve suportar evolução para CRM, histórico, retorno de clientes e demais recursos previstos no Documento Mestre.

### Produtos

- Cadastro/edição e Preços possuem referências formais UI-023 e UI-024.
- Não duplicar ação de “Salvar produto”.
- A navegação do produto deve contemplar as áreas Preço, Estoque e Fiscal.
- O produto deve evoluir sem romper requisitos de grade, tributação, estoque, combos e integrações previstos no Documento Mestre.

### PDV

- A direção visual do PDV Desktop já foi aceita e permanece como referência do catálogo.
- O desenho funcional deve comportar operação tradicional, últimas vendas/consulta, segunda tela do cliente, mesas/delivery e futuras modalidades de mínima intervenção.
- Requisitos offline, SQLite por terminal, impressão, contingência fiscal e sincronização são requisitos funcionais; não devem ser marcados como implementados apenas pela existência da tela.

### Fiscal e CRM

- As direções iniciais das telas Fiscal e CRM foram aceitas no processo visual e devem orientar as próximas revisões/implementações.
- O estado formal de cada arquivo continua sendo o registrado em `docs/design/README.md`; esta consolidação não promove automaticamente propostas para “visual aprovado”.

### Aplicativos

- Aplicativo do Cliente e Aplicativo do Vendedor possuem direções visuais catalogadas.
- App do vendedor deve suportar evolução para vendas, metas, comissão, promoções e produtos novos.

### Trial e Administração SaaS

- O trial de 7 dias pertence à página de prospecção/captação, não ao login administrativo do ERP.
- A Administração SaaS deve evoluir para gestão de clientes assinantes, planos, funcionalidades, situação financeira, cobrança, inadimplência, suporte e demais requisitos do Documento Mestre.

## Ordem visual/funcional de referência

A sequência definida para evolução das telas parte de Login e acesso e segue para ERP: Cliente, Produto, Estoque, Vendas, Financeiro, Relatórios e Configurações, conectando Fiscal, CRM/BI, PDV, aplicativos e administração SaaS conforme o roadmap oficial.

## Regra de rastreabilidade

Para evitar perda de contexto:

1. `docs/TRAXUP-DOCUMENTO-MESTRE.md` define escopo e roadmap do produto.
2. `docs/design/README.md` define o catálogo e o estado formal das referências visuais.
3. Este arquivo registra decisões visuais/funcionais consolidadas que precisam sobreviver ao arquivamento das conversas de design.
4. Código, backend, banco, CI e homologação precisam de evidência própria antes de uma funcionalidade ser considerada concluída.
