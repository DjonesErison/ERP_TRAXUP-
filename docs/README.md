# TRAXUP — Índice oficial de documentação

Este índice conecta as fontes oficiais do projeto sem duplicar requisitos.

## Fontes de verdade

| Assunto | Fonte oficial |
|---|---|
| Produto, jornadas e roadmap | [Documento Mestre](TRAXUP-DOCUMENTO-MESTRE.md) |
| Projeto Visual / telas aprovadas | [Catálogo visual](design/README.md) |
| Evidência de implementação | [Evidências](status/EVIDENCIAS-IMPLEMENTACAO.md) + código, migrations, testes e PRs |
| Governança Git e ambientes | [Etapa 0 — Governança Git](ETAPA-0-GOVERNANCA-GIT.md) |
| Arquitetura | [Arquitetura](arquitetura/README.md) e [ADRs](adr/) |
| Banco, negócio e testes | [Banco](banco/README.md), [Negócio](negocio/README.md), [Testes](testes/README.md) |

## Implementação do ERP

A implementação funcional é integrada em `develop` e promovida para `homologacao` somente após validação. `main` mantém a referência estável e a documentação consolidada.

- Backend: `tplug-erp/backend/`
- Frontend: `tplug-erp/frontend/`
- Infraestrutura: `tplug-erp/infra/`
- Documentação técnica histórica: `docs/tplug-erp/`

## Regra de interpretação

1. O Documento Mestre define **o que o produto deve fazer**.
2. O catálogo visual define **a referência de interface aprovada**.
3. Código + migrations + testes definem **o que está implementado**.
4. CI + SHA publicada definem **o que está em homologação**.
5. Validação do usuário define **o que foi aceito na homologação**.

Uma imagem aprovada não comprova implementação. Uma branch não comprova integração. Um PR aberto não comprova entrega.

## Etapas atuais x fases históricas

O acompanhamento atual usa **Etapas do roadmap consolidado**. Arquivos `docs/tplug-erp/fase-*.md` são registros históricos de ciclos anteriores e não devem ser usados para numerar as etapas atuais.

## Branches permanentes

- `main` — referência estável/documental;
- `develop` — integração de desenvolvimento;
- `homologacao` — versão candidata e ambiente de validação.

Branches de trabalho são temporárias. O fluxo, padrões e política de limpeza estão em [ETAPA-0-GOVERNANCA-GIT.md](ETAPA-0-GOVERNANCA-GIT.md).
