# Auditoria das telas TRAXUP — 15/10/2026

## Base confirmada

- Repositório exclusivo: DjonesErison/ERP_TRAXUP-.
- ERP: branch homologacao, commit publicado `765c0ce346fb52f98b2a832f9812e8e273467dca` (PR 417).
- Central: branch main, commit publicado `3507850a03659ad2d0455caf0a280e645ffb1f9a`.
- GET https://apphomologacao.traxup.com.br/build-info.json confirmou o commit do ERP nesta auditoria.
- CI/deploy da versão ERP: https://github.com/DjonesErison/ERP_TRAXUP-/actions/runs/38504966733. Backend, frontend/smoke, aprovação, publicação e deploy: success.
- GET sem sessão para produtos, vendas/pedidos/recentes, compras/pedidos, me/filiais e onboarding retornou HTTP 401 nesta auditoria, sem acesso público aos dados.
- Nenhuma alteração de tela, credencial, banco ou serviço da VPS foi feita nesta auditoria.

## Como ler os resultados

**Observado agora:** página aberta no navegador, sem autenticação, ou resposta HTTP atual.
**CI aprovado:** execução existente da versão publicada, com backend/migrations/build e testes de navegador. As interações dos smokes usam APIs simuladas; não comprovam todas as operações sobre o banco publicado.
**Parcial:** tela existe, mas há recursos indisponíveis ou apenas parte do fluxo foi testada.
**Não publicado:** não está no commit da homologação, ainda que exista imagem, protótipo, API ou PR aberto.

Não há sessão autenticada de teste disponível nesta revisão. Não foram criados tenants/produtos, enviados e-mails, alteradas senhas ou lançados movimentos reais. Portanto, nenhuma tela autenticada é classificada aqui como 100% validada em operação real. O navegador desta sessão não oferece redimensionamento; evidências mobile vêm dos smokes da versão publicada.

## ERP — links e arquivos

Base dos links: https://apphomologacao.traxup.com.br. Todos os arquivos abaixo ficam em `tplug-erp/frontend/src/app/`, na branch homologacao.

| Tela | Caminho/link | Arquivo principal | Resultado e limite |
| --- | --- | --- | --- |
| Login UI-016 | `/` e `/entrar` | app.component.ts/html/css | Observado agora: renderiza, Entrar bloqueado sem dados; CI cobre login simulado, erro, renovação e logout. Login com conta real não repetido. |
| Recuperação — solicitação | Modal em `/entrar`, botão Esqueci minha senha | app.component.ts/html, auth.service.ts; estilo src/recovery-modal.css | Modal e Voltar ao login observados agora. Entrega real do e-mail não repetida. |
| Recuperação — nova senha | `/recuperar` com fragmento de token válido | ativacao-admin.component.*, auth.service.ts | CI cobre token simulado, falha/sucesso e remoção do fragmento. Link sem token não é fluxo completo. |
| Trial UI-014 | `/trial`, alias `/teste`, e https://captacaoclientes.traxup.com.br/ | trial.component.*, trial.service.ts | Formulário e arte observados nos dois hosts. CI cobre sete larguras, validações, erros, recuperação de duplicidade, ativação simulada e PWA offline. Cadastro/e-mail real não repetido. Documentos legais pendentes. |
| Ativação UI-015 | `/ativar` com token válido | ativacao-admin.component.*, ativacao-admin.service.ts, reenviar-ativacao.component.ts | Página observada; botão desabilitado sem token/senha. CI cobre ativação simulada. Não confirma token/e-mail real. |
| Primeiro acesso UI-019 | `/configuracao` | onboarding.component.*, onboarding.service.ts | CI cobre dados do Trial, preenchimento/salvamento simulado, perfil fiscal, progresso e retorno às etapas. Recursos de equipe/PDV existem no código; operação real não revalidada. |
| Empresa/filial UI-018 | Seletor dentro do shell autenticado | app.component.*, contexto-operacional.service.ts | CI seleciona filial simulada; seleção múltipla real não repetida. Sem rota independente. |
| Dashboard UI-001 | `/dashboard` | dashboard.component.*, vendas.service.ts | Publicado. CI cobre últimos pedidos, filtros, filial, recarga, histórico e falhas em desktop/mobile. Faturamento/ticket/gráficos/alertas/downloads/IA permanecem pendentes. |
| Produtos UI-003 | `/produtos` | produtos.component.*, produtos.service.ts | Publicado; proteção de login observada. CI dedicado cobre cadastro, duplicidade, detalhes, filtros, CSV seguro, desativação e 403/vazio em 320/390/768/1536px. Edição/reativação/estoque/importação/etiquetas/combos/grade não integrados. |
| Vendas | `/vendas` | vendas.component.ts/css, vendas.service.ts | Consulta/lista, filtros, paginação e detalhes presentes; navegação/layout cobertos pelo smoke e parte da consulta por cenários simulados. Não é o PDV nem valida venda/faturamento real. |
| Compras | `/compras` | compras.component.ts/css, compras.service.ts | Consulta, criação, itens e recebimento presentes. Smoke verifica abertura/layout; operações de compra/estoque real não revalidadas. |
| Inventário | `/inventario` | inventario-mobile.component.ts, inventario.service.ts/models.ts | Sessões, leitura manual/câmera, contagem, conclusão/cancelamento/ajuste no código. Smoke verifica abertura/layout. Câmera física e ajustes reais não testados nesta auditoria. |
| Financeiro — conciliação | `/financeiro` | conciliacao-financeira.component.ts/css, conciliacao-financeira.service.ts | Conciliação, extrato OFX e sugestões presentes. Smoke verifica abertura/layout; importação/conciliação real não revalidada. Não equivale a todo o módulo financeiro. |
| Integrações financeiras | Dentro de `/financeiro` | integracoes-financeiras.component.ts/css | Cadastro, histórico e sincronização no código; disponibilidade de adaptadores externos e operação real pendentes de validação. Sem rota independente. |
| Contabilidade | `/contabilidade` | contabilidade.component.ts/css, contabilidade.service.ts | Fechamento/checklist/pacote presentes; abertura/layout em CI. Geração/fechamento real não repetidos. |
| XML/SPED | Dentro de `/contabilidade` | contabilidade-arquivos.component.ts/css | Consulta/exportação no código; operação real não revalidada. Sem rota independente. |
| Livro-caixa/inventários contábeis | Dentro de `/contabilidade` | contabilidade-movimentos.component.ts/css | Consulta/paginação/exportação presentes; abertura do contêiner em CI. Exportações reais não repetidas. |
| CRM | `/crm` | app.component.ts/html, crm.service.ts/models.ts | Indicadores de inativos/RFV/follow-ups/interações. Navegação/layout em CI. Não é cadastro de clientes nem campanha operacional completa. |

HTTP 200 foi confirmado em todos os caminhos acima e também em `/clientes`. Isso é o fallback do SPA: as respostas HTML têm o mesmo tamanho. **HTTP 200 não prova que um módulo existe.** `/produtos` sem sessão foi aberto no navegador e mostrou login, sem expor o catálogo.

## Onboarding — sub-telas e diferença encontrada

O código publicado exibe: 1 Dados da empresa; 2 Configuração fiscal; 3 Usuários e permissões; 4 Vendas e PDV. A ordem solicitada posteriormente (Usuários como item 2 e Fiscal como item 3) **não está aplicada**.

- Dados da empresa vêm do Trial e permitem completar endereço/filial.
- Fiscal salva perfil; certificado digital/CSC e emissão não ficam validados por essa etapa.
- Equipe lista usuários/perfis e permite atribuir perfil; não constitui todas as telas de administração de usuários.
- PDV configura terminal/impressão/regras; não é a tela de venda do PDV Desktop.
- Botões de retorno ao painel estão implementados e cobertos pelo smoke de configuração.

## Central — navegação observada agora

Host: https://central.traxup.com.br. Fonte na branch main, não na pasta do ERP: `src/app/`.

| Caminho | Fonte | Resultado |
| --- | --- | --- |
| `/`, `/dashboard` | pages/dashboard.component.ts | Dashboard abre, exibe progresso e build 3507850a. Percentuais são do backlog versionado, não porcentagens de telas validadas nesta revisão. |
| `/modulos` | pages/modules.component.ts | Catálogo abre; conteúdo de acompanhamento. |
| `/funcionalidades` | pages/functionalities.component.ts | Backlog/evidências abrem. Campos de filtro não têm binding no componente: não filtram a lista. |
| `/funcionalidades/nova` | pages/feature-form.component.ts | Formulário no código. Salvar apenas alert de protótipo; Gerar documentação apenas alert. Não persistem cadastro/documentos no backend/GitHub. |
| `/ideias` | pages/ideas.component.ts | Quadro abre; dados definidos no código. |
| `/documentacao` | pages/documentation.component.ts | Página abre; catálogo documental. |
| `/roadmap` | pages/roadmap.component.ts | Página abre; fases versionadas. |
| `/referencias` | pages/references.component.ts | Galeria abre. Referência visual não comprova implementação; lista inclui imagens de módulos ainda ausentes do ERP. |
| `/regras`, `/dependencias`, `/testes`, `/historico`, `/github`, `/configuracoes` | pages/simple-page.component.ts | Todas abertas no navegador. Páginas informativas, não painéis administrativos integrados à VPS/GitHub. |

A busca global do shell da Central também é um input sem lógica de consulta. Não houve teste de gravação na Central; o comportamento de não persistência acima foi confirmado no código.

## Não publicados / ainda indisponíveis

- **Clientes:** PR https://github.com/DjonesErison/ERP_TRAXUP-/pull/418 aberto, base homologacao, branch feat/clientes-ui022. Não integra o commit publicado. Consulta/cadastro/360º desse PR não foram tratados como implementados em produção nesta revisão.
- Produtos: editar/reativar, importação, etiquetas, saldos/alertas de estoque, combos e grade permanecem pendentes.
- Dashboard: totais monetários agregados, ticket, gráficos, pagamentos, alertas e downloads não integrados.
- Fiscal completo, BI, Relatórios e Assistente IA permanecem desabilitados no menu ERP. APIs específicas existentes não equivalem a telas completas.
- PDV Desktop UI-011 existe como protótipo em main; não há rota de venda PDV no shell homologacao.
- Aplicativos cliente/vendedor, administração SaaS e demais imagens da galeria não são telas publicadas por estarem desenhadas.

## Localização correta no GitHub

| Conteúdo | Branch e pasta |
| --- | --- |
| Aplicação ERP publicada | homologacao: `tplug-erp/frontend/src/app/` |
| APIs ERP | homologacao: `tplug-erp/backend/src/main/java/com/traxup/tplug/erp/` |
| Banco/migrations | homologacao: `tplug-erp/backend/src/main/resources/db/migration/` |
| Testes backend | homologacao: `tplug-erp/backend/src/test/` |
| Smokes/layout/API simulada | homologacao: `tplug-erp/infra/homologacao/` — visual-smoke.mjs, trial-smoke.mjs, configuracao-smoke.mjs, produtos-smoke.mjs |
| Workflow ERP | homologacao: `.github/workflows/homologacao.yml` |
| Publicação ERP | homologacao: `tplug-erp/infra/homologacao/deploy.sh`, ghcr-deploy.sh, compose.yml, compose.mail.yml |
| Documentos recentes de implementação | homologacao: `docs/status/` |
| Central publicada | main: `src/app/`, `src/app/pages/`, `src/app/data/` |
| Protótipos aprovados existentes | main: `Prototipo Telas/UI-001-Dashboard-ERP`, `UI-003-Produtos`, `UI-011-PDV-Desktop` |
| Imagens/identidade 2.0 | main: `docs/design/13-identidade-visual-2.0/`; logo oficial `Logo.png` |
| Outras referências de telas | main: `docs/design/`; referência apenas, não prova de implantação |

A pasta Prototipo Telas não existe na árvore publicada de homologacao; ela está na main. O projeto Angular da raiz de main é a Central, não o frontend ERP. Essa distinção deve ser conferida antes de cada alteração.

## Pendências visuais e de navegação identificadas no código

1. Ordenação dos cards 2/3 do onboarding difere do pedido (ver acima).
2. Shell autenticado do ERP ainda usa `assets/traxup-logo.webp`, enquanto login/onboarding/ativação usam Logo.png; exige conferência com a regra da logo oficial, sem substituir por outra arte.
3. Trial desktop depende de imagem remota raw.githubusercontent.com na main; nesta revisão carregou após a primeira captura, mas a dependência externa existe. Não foi diagnosticada falha persistente da imagem.
4. O botão Ainda não é cliente no login muda a tela por estado, sem atualizar o endereço; retorno do trial ao login também altera estado. Links diretos /trial e /entrar existem, mas nem todas as transições públicas sincronizam URL.
5. Políticas de privacidade/termos TRAXUP continuam pendentes. Os links de outras empresas foram removidos.
6. Fonte da Central é estática em diversos locais; os números não devem ser apresentados como medição operacional de cada tela.

## Próxima validação necessária

Com conta de teste e permissões adequadas, percorrer na VPS: cadastro trial → entrega de e-mail → ativação → login → configuração → filial → dashboard → criação/consulta/desativação de produto. Depois testar compras/inventário/financeiro/contabilidade com massa controlada e conferir efeitos no banco. Usar autenticação segura; nunca solicitar senha/token em chat. A auditoria atual não autoriza marcar esses fluxos reais como concluídos.

Esta revisão produz inventário e evidências; correções devem seguir PR próprio e merge somente autorizado com CI verde. Não alterar main ou outros serviços.
