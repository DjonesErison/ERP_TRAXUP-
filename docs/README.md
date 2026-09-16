# TRAXUP — Índice de documentação

Este índice conecta as fontes existentes. O [Documento Mestre](TRAXUP-DOCUMENTO-MESTRE.md) mantém a visão do produto, as jornadas e o roadmap oficial; este índice não substitui nem reproduz seu conteúdo.

## Produto, visual e acompanhamento — main

| Assunto | Fonte |
|---|---|
| Produto e jornadas | [Documento Mestre](TRAXUP-DOCUMENTO-MESTRE.md) |
| Sequência de implementação | [Roadmap funcional oficial](TRAXUP-DOCUMENTO-MESTRE.md#roadmap-funcional-de-alto-nível) |
| Projeto Visual e aprovação das telas | [Catálogo visual](design/README.md) |
| Evidências | [Implementação](status/EVIDENCIAS-IMPLEMENTACAO.md) e [fluxo fiscal](status/EVIDENCIAS-FLUXO-FISCAL.md) |
| Dados usados no acompanhamento da Central | [Funcionalidades, pesos e evidências](../src/app/data/project-progress.ts) |
| Arquitetura da Central | [Arquitetura](arquitetura/README.md) |
| Decisões arquiteturais | [ADRs](adr/) |
| Documentação da API da Central | [API](api/README.md) |
| Banco, negócio e qualidade | [Banco](banco/README.md), [regras](negocio/README.md) e [testes](testes/README.md) |
| Operação da Central | [Deploy](../deploy/README.md) e [workflows](../.github/workflows/) |

## Implementação do ERP — develop

Os links abaixo apontam para a base de implementação. Não copiar esses documentos para criar versões paralelas em `main`.

| Assunto | Fonte |
|---|---|
| Estado técnico e índice das fases | [Documentação técnica](https://github.com/DjonesErison/ERP_TRAXUP-/blob/develop/docs/tplug-erp/README.md) |
| Arquitetura atual | [Monólito modular](https://github.com/DjonesErison/ERP_TRAXUP-/blob/develop/docs/tplug-erp/arquitetura-atual.md) |
| Segurança e isolamento | [Segurança e multitenancy](https://github.com/DjonesErison/ERP_TRAXUP-/blob/develop/docs/tplug-erp/seguranca-e-multitenancy.md) |
| Banco | [Inventário de migrations](https://github.com/DjonesErison/ERP_TRAXUP-/blob/develop/docs/tplug-erp/migrations.md) |
| Ambientes e operação | [Ambientes e deploy](https://github.com/DjonesErison/ERP_TRAXUP-/blob/develop/docs/tplug-erp/ambientes-e-deploy.md) |
| Código | [Backend](https://github.com/DjonesErison/ERP_TRAXUP-/tree/develop/tplug-erp/backend), [frontend](https://github.com/DjonesErison/ERP_TRAXUP-/tree/develop/tplug-erp/frontend) e [infraestrutura](https://github.com/DjonesErison/ERP_TRAXUP-/tree/develop/tplug-erp/infra) |

## Como interpretar o estado do projeto

- O Documento Mestre define o produto e seu roadmap.
- O catálogo visual registra referências e situações de aprovação; aprovação visual não comprova implementação.
- Os documentos de evidências e os links para código/PRs sustentam o acompanhamento. Implementação parcial não equivale a homologação ou produção.
- Os READMEs técnicos descrevem o ERP; os documentos da Central descrevem a ferramenta de acompanhamento.

## Continuidade — Etapa 0 de organização

Inventário de referência em 15/09/2026: `main` no commit `1311791cb822050af110af9ecf935d1c6e8296b4` e `develop` em `6d5b76e1c7f3ae1f374075fa2625b356310b0236`. A primeira mudança organiza índices e referências, mantendo os caminhos existentes.

Pendências da consolidação:

1. Alinhar a página de Roadmap da Central à sequência do Documento Mestre; atualmente há fases e percentuais mantidos separadamente.
2. Unificar a manutenção do catálogo visual e sua apresentação na Central, preservando códigos UI, imagens e situações.
3. Resolver individualmente as divergências entre `main` e `develop`, validando workflows, Docker, deploy e referências antes de qualquer movimentação.
4. Reconciliar decisões ainda restritas às conversas antes de declarar a consolidação concluída.

A [PR #324](https://github.com/DjonesErison/ERP_TRAXUP-/pull/324) permanece fora desta reorganização. Esta Etapa 0 de organização não deve ser confundida com a Fase 0 técnica descrita na documentação do ERP.
