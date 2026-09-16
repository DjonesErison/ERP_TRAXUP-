# TRAXUP — Etapa 0 — Auditoria Final de Consolidação

## Objetivo

Registrar a fonte de verdade consolidada do projeto e impedir que especificação visual, requisito ou documentação sejam confundidos com implementação funcional.

## Fontes oficiais

1. `docs/TRAXUP-DOCUMENTO-MESTRE.md` — escopo, jornadas, arquitetura funcional e roadmap do produto.
2. `docs/design/README.md` — catálogo e estado formal das referências visuais.
3. `docs/design/DECISOES-VISUAIS-CONSOLIDADAS.md` — decisões visuais/funcionais preservadas das conversas de design.
4. `src/app/data/project-progress.ts` — painel rastreável da Central; seus percentuais são indicadores ponderados e não prova isolada de conclusão.
5. `develop` / `tplug-erp/` — implementação funcional em evolução do ERP.
6. `homologacao` — versão estável destinada a validação funcional do usuário após CI/deploy aprovado.

## Mapa de responsabilidade

| Área | Fonte principal | Regra |
|---|---|---|
| Central, documentação e catálogo visual | `main` | Não substituir evidência de implementação |
| ERP Web / backend / banco / integrações | `develop` / `tplug-erp/` | Exigir código + testes/CI compatíveis com o escopo |
| Versão para teste do usuário | `homologacao` | Somente promover funcionalidade suficientemente validada |
| Produção | fluxo posterior à homologação | Não promover automaticamente |

## Correções de interpretação encontradas na auditoria

### PDV Offline e Sincronização

Os itens `PDV-OFF` e `PDV-SYNC` aparecem no tracker da Central como **Em implementação**, porém a auditoria da Etapa 0 não encontrou evidência concreta equivalente de implementação SQLite/offline/sincronização no código auditado. Portanto, esse status não deve ser usado como prova de implementação. Até existir evidência compatível, tratá-los operacionalmente como requisitos especificados/pendentes de comprovação.

### Redis e Kafka

Permanecem arquitetura especificada. Não há evidência concreta suficiente para declará-los implementados.

### Fiscal

Existe implementação backend relevante e homologação simulada em vários subfluxos. Isso não equivale a integração real completa com SEFAZ. Comunicação oficial, autorização real, DANFE e fechamento de produção exigem evidência própria.

### Catálogo visual

A existência ou aprovação de uma imagem não comprova Angular, backend, banco ou integração. O estado formal de cada referência permanece no catálogo visual.

## Fluxo oficial após a Etapa 0

`requisito → tela aprovada → Angular → tela funcional → backend → banco → integração → testes/CI → homologacao → teste do usuário → aprovação → concluído`

Etapas não aplicáveis a uma funcionalidade podem ser omitidas, mas nunca se deve marcar conclusão sem evidência compatível com o escopo declarado.

## Regra de homologação

Cada nova funcionalidade suficientemente finalizada deve ser integrada em `develop`, validada por CI e promovida para `homologacao`. Após deploy verde, o usuário recebe o aviso **“já pode testar”** com o acesso de homologação. A aprovação do usuário fecha o ciclo funcional antes da próxima promoção relevante.

## Resultado da consolidação

A Etapa 0 considera consolidado o conhecimento estrutural, funcional e visual necessário para continuidade do projeto no GitHub. Pendências de implementação permanecem no roadmap e não são convertidas artificialmente em funcionalidades concluídas.
