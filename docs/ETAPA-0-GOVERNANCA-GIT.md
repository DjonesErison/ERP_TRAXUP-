# Etapa 0 — Governança Git do TRAXUP

## Objetivo

Manter o repositório simples, rastreável e rápido para desenvolvimento, homologação e validação do TRAXUP.

## Branches permanentes

Somente três branches têm papel permanente:

- `main`: documentação consolidada, referências oficiais e linha estável do projeto.
- `develop`: integração do desenvolvimento funcional.
- `homologacao`: versão candidata/publicada para validação do usuário.

Branches de trabalho são temporárias e devem usar apenas estes prefixos:

- `feat/<bloco>` para funcionalidade;
- `fix/<bloco>` para correção;
- `docs/<bloco>` para documentação;
- `chore/<bloco>` para manutenção estrutural.

## Fluxo padrão

Para cada bloco funcional:

1. auditar dependências e contratos antes de codificar;
2. criar uma única branch de trabalho;
3. implementar o bloco completo, incluindo backend, frontend, migrations e testes necessários;
4. abrir um único PR para a branch de integração adequada;
5. exigir CI verde;
6. promover para `homologacao` em um único ciclo;
7. validar no ambiente publicado;
8. remover a branch temporária após merge.

Evitar PR por ajuste pequeno quando os ajustes pertencem ao mesmo bloco funcional.

## Regras de homologação

- `homologacao` nunca recebe desenvolvimento experimental direto.
- promoção somente por PR e CI verde;
- usar a label `homologacao-aprovada` conforme workflow vigente;
- falha de CI bloqueia merge;
- após deploy, validar SHA e smoke público antes de declarar publicado.

## Fonte de verdade

- Produto/roadmap: `docs/TRAXUP-DOCUMENTO-MESTRE.md`.
- Visual: `docs/design/`.
- Implementação: código, migrations e testes.
- Publicação: workflow/CI + SHA da homologação.

Aprovação visual não equivale a implementação. Documento de planejamento não equivale a código entregue.

## Nomenclatura histórica

Arquivos `docs/tplug-erp/fase-*.md` representam ciclos históricos da implementação e não são a numeração das **Etapas do roadmap consolidado atual**. O acompanhamento atual deve usar explicitamente a palavra `Etapa`.

## Política de limpeza

Após merge e confirmação de que o conteúdo está preservado nas branches permanentes:

- fechar PRs substituídos/obsoletos;
- excluir branches temporárias mergeadas;
- não excluir `main`, `develop` ou `homologacao`;
- não excluir branch com PR aberto válido ou conteúdo ainda não incorporado;
- manter o histórico dos PRs fechados como evidência, sem manter suas branches indefinidamente.

## Estado da auditoria de 17/09/2026

A Etapa 0 foi reavaliada porque o repositório acumulou grande quantidade de branches temporárias e divergências históricas entre as linhas permanentes. A infraestrutura de CI/homologação está funcional, mas a organização deve ser saneada antes de retomar novas etapas funcionais.

A limpeza deve ser conservadora: primeiro fechar PRs claramente substituídos, depois remover apenas branches comprovadamente mergeadas/obsoletas. Branches antigas com conteúdo não verificado ficam preservadas até comparação explícita.
