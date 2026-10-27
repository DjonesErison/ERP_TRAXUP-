# UI-030 — Onboarding: dados da empresa multi-loja

## Status

- Referência visual aprovada em 27/10/2026.
- Arquivo: `UI-030-onboarding-dados-empresa-multiloja-traxup.webp`.
- Escopo: referência visual e funcional; não contém implementação.

## Objetivo

Iniciar o onboarding após ativação e login, recuperando os dados informados no Trial e apresentando a quantidade de lojas declarada.

## Regras funcionais

- Carregar código público da empresa, nome fantasia, razão social, documento, segmento, responsável, e-mail e telefone.
- O código da empresa deve ser somente leitura.
- A quantidade de lojas vem do Trial e define quantas unidades serão configuradas.
- Uma conta representa um tenant/empresa; as lojas são estabelecimentos ou filiais desse tenant.
- Para três lojas, o fluxo deve prever uma matriz e duas filiais.

## Etapas

1. Empresa.
2. Lojas.
3. Unidade.
4. Revisão.
5. Acesso.

