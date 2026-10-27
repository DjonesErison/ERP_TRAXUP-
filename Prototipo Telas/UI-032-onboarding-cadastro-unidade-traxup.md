# UI-032 — Onboarding: cadastro da unidade

## Status

- Referência visual aprovada em 27/10/2026.
- Arquivo: `UI-032-onboarding-cadastro-unidade-traxup.webp`.
- Escopo: referência visual e funcional; não contém implementação.

## Objetivo

Cadastrar ou editar uma matriz/filial com dados cadastrais, endereço, informações fiscais e opções operacionais.

## Campos e grupos

- Identificação: nome, tipo, CNPJ e inscrição estadual.
- Endereço: CEP, logradouro, número, cidade e UF.
- Responsável e telefone.
- Fiscal: regime tributário e configurações específicas da unidade.
- Operação: fuso horário, estoque próprio, PDV e emissão fiscal.

## Regras funcionais

- Validar unicidade e formato do documento da unidade.
- Permitir copiar apenas configurações operacionais da matriz.
- Salvar e retornar à lista de lojas ou avançar para a próxima unidade.
- Registrar situação `Em configuração`, `Configurada` ou `Pendente`.

