# APIs

Este diretório registra o estado das APIs do ecossistema TRAXUP.

## TPlug ERP

Backend atual: Java 21 + Spring Boot, REST, autenticação JWT e isolamento multi-tenant pelo claim `tenant_id`.

Endpoints atualmente implementados:

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `/api/v1/empresas`
- `/api/v1/filiais`
- `/api/v1/usuarios`
- `GET /api/v1/auditorias` — requer `AUDITORIA_LER` e retorna somente eventos do tenant autenticado.

A autorização dos endpoints operacionais usa authorities derivadas do claim `permissions` do JWT. OpenAPI/Swagger ainda está planejado e não deve ser considerado implantado.

## TRAXUP Central

A Central continua sendo uma aplicação separada de governança/produto. Sua integração com backend não faz parte desta entrega.
