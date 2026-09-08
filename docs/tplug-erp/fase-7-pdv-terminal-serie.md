# Fase 7 — Bloco 1: terminal e série do PDV

Implementação inicial do contrato de terminal do PDV.

## Persistência

A migration `V62` cria `pdv_terminais` com:

- vínculo obrigatório a tenant e filial;
- `codigo` estável e único por tenant;
- `serie` positiva e única por filial dentro do tenant;
- nome operacional;
- estado ativo/inativo;
- timestamps de criação e atualização;
- FK composta tenant-safe para filial.

## API

Base: `/api/v1/pdv/terminais`.

- `GET /api/v1/pdv/terminais` — lista terminais, com filtro opcional por filial;
- `GET /api/v1/pdv/terminais/{terminalId}` — consulta terminal;
- `POST /api/v1/pdv/terminais` — cria terminal;
- `POST /api/v1/pdv/terminais/{terminalId}/ativar` — ativa terminal;
- `POST /api/v1/pdv/terminais/{terminalId}/desativar` — desativa terminal.

## Segurança

- `PDV_TERMINAL_LER` protege consultas;
- `PDV_TERMINAL_GERENCIAR` protege mutações;
- tenant vem exclusivamente do `TenantContext` autenticado;
- filial é validada dentro do tenant;
- código e série duplicados são rejeitados;
- criação, ativação e desativação são auditadas.

## Identidade para sincronização

O identificador UUID do terminal e seu `codigo` estável formam a identidade lógica usada pelos próximos blocos. A série é propriedade do terminal na filial e não deve ser reaproveitada por outro terminal ativo ou inativo enquanto o registro existir.

Este bloco não implementa SQLite local nem sincronização de vendas; isso pertence ao Bloco 2 da Fase 7.
