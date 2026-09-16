# TRAXUP — Produto, Central e implementação

Este repositório reúne o planejamento do TRAXUP, as referências visuais, a Central de acompanhamento e a implementação do ERP. Comece pelo [índice de documentação](docs/README.md).

## Onde está cada parte

| Área | Fonte existente | Base |
|---|---|---|
| Produto, jornadas e roadmap oficial | [Documento Mestre](docs/TRAXUP-DOCUMENTO-MESTRE.md) | `main` |
| Projeto Visual | [Catálogo e situações das telas](docs/design/README.md) | `main` |
| Evidências de implementação | [Evidências](docs/status/EVIDENCIAS-IMPLEMENTACAO.md) e [fluxo fiscal](docs/status/EVIDENCIAS-FLUXO-FISCAL.md) | `main` |
| Central Angular | [Interface](src/) e [dados de acompanhamento](src/app/data/project-progress.ts) | `main` |
| Implementação do ERP | [Backend](https://github.com/DjonesErison/ERP_TRAXUP-/tree/develop/tplug-erp/backend), [frontend](https://github.com/DjonesErison/ERP_TRAXUP-/tree/develop/tplug-erp/frontend) e [documentação técnica](https://github.com/DjonesErison/ERP_TRAXUP-/blob/develop/docs/tplug-erp/README.md) | `develop` |
| Operação da Central | [Deploy](deploy/README.md) e [workflows](.github/workflows/) | `main` |

As bases ainda contêm conjuntos diferentes. Os links para a implementação usam `develop` explicitamente; sua ausência em `main` não significa ausência no projeto. A Central na raiz e o frontend operacional em `tplug-erp/frontend` são aplicações distintas.

O Documento Mestre continua sendo a referência do roadmap. Imagem aprovada, código implementado, teste aprovado e entrega em produção são estados diferentes; consulte as evidências antes de considerar uma funcionalidade concluída.

## Executar a Central

```bash
npm ci
npm start
```

Acesse o endereço informado pelo Angular CLI.

## Telas já criadas

- Dashboard
- Módulos
- Funcionalidades
- Nova Funcionalidade
- Ideias / Kanban
- Documentação
- Roadmap
- Regras de Negócio
- Referências / Imagens
- Dependências
- Testes
- Histórico
- GitHub
- Configurações

## Integração da Central

A integração da Central com autenticação, persistência e serviços é uma frente própria. Consulte a [documentação técnica do ERP](https://github.com/DjonesErison/ERP_TRAXUP-/blob/develop/docs/tplug-erp/README.md) para o estado da implementação operacional.
