# TPlug ERP Frontend

Frontend web do TPlug ERP em Angular 19, separado da TRAXUP Central.

## Autenticacao

O login visual usa o contrato real do backend:

- `POST /api/v1/auth/login` com `tenantId`, `email` e `senha`;
- `POST /api/v1/auth/refresh` para rotacao do refresh token;
- `POST /api/v1/auth/logout` para revogar a sessao atual.

Access token, refresh token e o ultimo tenant utilizado ficam no armazenamento local do navegador. O interceptor adiciona `Authorization: Bearer` apenas fora das rotas `/api/v1/auth/*` e, em `401`, executa uma unica renovacao compartilhada para evitar corridas quando varias requisicoes falham simultaneamente.

O tenant continua sendo derivado e validado pelo backend a partir do JWT. O frontend nao envia `X-Tenant-Id` como fonte de autoridade.

## CRM

O painel operacional consome:

- `GET /api/v1/crm/clientes/inativos`;
- `GET /api/v1/crm/clientes/rfv`;
- `GET /api/v1/crm/followups`;
- `GET /api/v1/crm/interacoes`.

O frontend usa URLs relativas `/api/v1/...`, permitindo reverse proxy no mesmo host em homologacao/producao.

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
