# TPlug ERP — Documentação Técnica Consolidada

Este diretório registra o estado técnico atual do TPlug ERP. O GitHub é a fonte de verdade para código e documentação técnica; a TRAXUP Central permanece como ferramenta de governança do produto.

## Estado atual

A base do backend está em Spring Boot 4.1.1 com Java 21, PostgreSQL 17, Flyway, Spring Security, JWT com refresh token, RBAC, auditoria multi-tenant e OpenAPI/Swagger opt-in.

A arquitetura segue monólito modular. Regras de negócio ficam no backend e todo acesso operacional deve respeitar isolamento por tenant.

## Componentes já implementados

- Fundação de tenant, empresa e filial.
- Usuários por tenant.
- Autenticação JWT com refresh token rotativo e revogação.
- Tenant derivado exclusivamente do claim `tenant_id` do JWT.
- RBAC com perfis e catálogo global de permissões.
- API administrativa de RBAC protegida por `RBAC_GERENCIAR`.
- Proteção por permissão nos endpoints de Empresa, Filial e Usuário.
- Auditoria multi-tenant e registro automático de operações críticas de criação/desativação em Empresa, Filial e Usuário.
- Consulta de auditoria protegida por `AUDITORIA_LER` e isolada pelo tenant do JWT.
- Bootstrap seguro e opt-in do primeiro tenant/administrador, sem endpoint público.
- OpenAPI/Swagger disponível de forma opt-in para documentação da API.
- PostgreSQL com migrations Flyway V1 a V45; o inventário vigente está em `migrations.md`.
- CI do backend no GitHub Actions com Java 21 e PostgreSQL 17 efêmero.
- Dockerfile multi-stage do backend com runtime Java 21 não-root e validação de build da imagem no CI.
- Contrato de variáveis de ambiente documentado em `.env.example`.
- Estratégia de ambientes, promoção, deploy e rollback formalizada.

## Segurança

- Senhas armazenadas com BCrypt.
- Refresh tokens armazenados somente por hash SHA-256.
- JWT assinado por segredo externo ao código.
- `X-Tenant-Id` não é fonte confiável de tenant.
- Permissões efetivas são carregadas no JWT como authorities.
- Auditoria não deve armazenar senha, token bruto, segredo JWT ou credencial.
- Bootstrap administrativo é desabilitado por padrão, depende de variáveis de ambiente e só pode executar quando ainda não existe tenant cadastrado.
- A imagem Docker executa a aplicação com usuário não-root.
- Segredos de desenvolvimento, homologação e produção não devem ser versionados nem compartilhados entre ambientes.

## Bootstrap do primeiro administrador

O bootstrap não expõe rota HTTP. Para uma instalação nova, defina temporariamente:

- `BOOTSTRAP_ADMIN_ENABLED=true`
- `BOOTSTRAP_ADMIN_TENANT_NAME`
- `BOOTSTRAP_ADMIN_NAME`
- `BOOTSTRAP_ADMIN_EMAIL`
- `BOOTSTRAP_ADMIN_PASSWORD` com no mínimo 12 caracteres

Na primeira inicialização, o backend cria o tenant, o usuário administrador com BCrypt, o perfil `ADMIN`, associa todas as permissões existentes ao perfil e vincula o usuário ao perfil. Após a criação inicial, remova/desabilite as variáveis de bootstrap. Se já existir qualquer tenant, uma nova tentativa é recusada.

## Build da imagem Docker

A partir de `tplug-erp/backend`:

```bash
docker build -t tplug-erp-backend:local .
```

A aplicação continua recebendo banco, JWT e demais configurações por variáveis de ambiente; nenhuma credencial é incorporada à imagem.

## Fase 0

Os itens técnicos previstos para a Fase 0 estão implementados ou formalizados no repositório: segurança multi-tenant, autenticação, RBAC, auditoria, bootstrap inicial, OpenAPI opt-in, imagem Docker, CI e contrato de ambientes/deploy.

## Documentos deste diretório

- `arquitetura-atual.md`: arquitetura e decisões vigentes.
- `seguranca-e-multitenancy.md`: autenticação, autorização, isolamento e auditoria.
- `migrations.md`: inventário das migrations imutáveis.
- `ambientes-e-deploy.md`: contrato de ambientes, promoção, deploy, checklist e rollback.
