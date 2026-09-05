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
- `GET /api/v1/rbac/perfis` — requer `RBAC_GERENCIAR` e lista somente perfis do tenant autenticado.
- `GET /api/v1/rbac/permissoes` — requer `RBAC_GERENCIAR` e lista o catálogo global de permissões.
- `POST /api/v1/rbac/perfis` — requer `RBAC_GERENCIAR` e cria perfil no tenant autenticado.
- `POST /api/v1/rbac/perfis/{perfilId}/permissoes/{permissaoId}` — requer `RBAC_GERENCIAR` e vincula permissão ao perfil do mesmo tenant.
- `POST /api/v1/rbac/usuarios/{usuarioId}/perfis/{perfilId}` — requer `RBAC_GERENCIAR` e vincula perfil ao usuário do mesmo tenant.

A autorização dos endpoints operacionais usa authorities derivadas do claim `permissions` do JWT. A API administrativa de RBAC nunca aceita tenant por header ou payload: o tenant é derivado exclusivamente do JWT e os vínculos entre tenants diferentes são rejeitados.

### OpenAPI / Swagger

A documentação automática usa `springdoc-openapi` 3.0.2 e fica desabilitada por padrão. Para habilitar em um ambiente autorizado, configure:

```bash
OPENAPI_ENABLED=true
```

Com a documentação habilitada:

- `GET /v3/api-docs` — documento OpenAPI em JSON.
- `/swagger-ui.html` — Swagger UI.

O esquema de segurança `bearerAuth` usa JWT Bearer. A exposição da documentação deve ser decidida por ambiente; produção deve mantê-la desabilitada salvo necessidade operacional explícita.

## TRAXUP Central

A Central continua sendo uma aplicação separada de governança/produto. Sua integração com backend não faz parte desta entrega.
