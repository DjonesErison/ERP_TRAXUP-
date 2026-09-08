# TPlug ERP Frontend

Frontend web do TPlug ERP em Angular 19, separado da TRAXUP Central.

## Primeira entrega

A tela inicial e o painel operacional de CRM e consome:

- `GET /api/v1/crm/clientes/inativos`;
- `GET /api/v1/crm/clientes/rfv`;
- `GET /api/v1/crm/followups`;
- `GET /api/v1/crm/interacoes`.

O frontend usa URLs relativas `/api/v1/...`, permitindo reverse proxy no mesmo host em homologacao/producao.

Enquanto o fluxo visual de login ainda nao estiver implementado, o interceptor busca o JWT em `localStorage` pela chave `tplug_access_token`. O tenant continua sendo derivado pelo backend a partir do JWT; o frontend nao envia `X-Tenant-Id` como fonte de autoridade.

## Desenvolvimento

```bash
npm install
npm start
```

## Build

```bash
npm run build
```

O workflow `TPlug ERP Frontend - CI` executa instalacao das dependencias e build Angular em Node 20 para PRs e pushes que alterem `tplug-erp/frontend`.
