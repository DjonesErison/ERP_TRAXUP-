# UI-034 — Seleção de empresa e filial

## Status

- Referência visual aprovada em 27/10/2026.
- Arquivo: `UI-034-selecao-empresa-filial-traxup.webp`.
- Escopo: referência visual e funcional; não contém implementação.

## Objetivo

Permitir que o usuário escolha a empresa e a filial autorizada antes de acessar o Dashboard do ERP.

## Regras funcionais

- Mostrar somente empresas e filiais autorizadas para o usuário autenticado.
- Exibir matriz, filiais, cidade/UF, situação e pendências.
- Destacar a última filial utilizada.
- Permitir memorizar a filial para os próximos acessos.
- Filial com pendência deve mostrar a restrição sem ocultar a unidade.
- `Entrar no ERP` deve definir o contexto tenant/filial e abrir o Dashboard.
- A troca posterior de filial deve estar disponível no cabeçalho do ERP.

## Fluxo relacionado

Trial → Ativação → Login → Onboarding multi-loja → Seleção de filial → Dashboard.

