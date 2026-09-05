# TPlug ERP — Documentação Técnica Consolidada

Este diretório registra o estado técnico atual do TPlug ERP. O GitHub é a fonte de verdade para código e documentação técnica; a TRAXUP Central permanece como ferramenta de governança do produto.

## Estado atual

A base do backend está em Spring Boot 4.1.1 com Java 21, PostgreSQL 17, Flyway, Spring Security, JWT com refresh token, RBAC e auditoria multi-tenant.

A arquitetura segue monólito modular. Regras de negócio ficam no backend e todo acesso operacional deve respeitar isolamento por tenant.

## Componentes já implementados

- Fundação de tenant, empresa e filial.
- Usuários por tenant.
- Autenticação JWT com refresh token rotativo e revogação.
- Tenant derivado exclusivamente do claim `tenant_id` do JWT.
- RBAC com perfis e catálogo global de permissões.
- Proteção por permissão nos endpoints de Empresa e Filial.
- Auditoria multi-tenant e registro automático de operações críticas de criação/desativação em Empresa, Filial e Usuário.
- PostgreSQL com migrations Flyway V1 a V7.
- CI do backend no GitHub Actions com Java 21 e PostgreSQL 17 efêmero.

## Segurança

- Senhas armazenadas com BCrypt.
- Refresh tokens armazenados somente por hash SHA-256.
- JWT assinado por segredo externo ao código.
- `X-Tenant-Id` não é fonte confiável de tenant.
- Permissões efetivas são carregadas no JWT como authorities.
- Auditoria não deve armazenar senha, token bruto, segredo JWT ou credencial.

## Próximos itens da Fase 0

- Proteger endpoints de Usuário com permissões específicas.
- Definir bootstrap seguro do primeiro administrador do tenant.
- Criar API de administração de RBAC protegida por `RBAC_GERENCIAR`.
- Expor consulta de auditoria com autorização adequada.
- Consolidar OpenAPI/Swagger após validar compatibilidade com Spring Boot 4.1.1.
- Criar Dockerfile e fluxo de imagem do backend.
- Revisar versões das actions do CI.
- Formalizar configuração de ambientes e deploy.

## Documentos deste diretório

- `arquitetura-atual.md`: arquitetura e decisões vigentes.
- `seguranca-e-multitenancy.md`: autenticação, autorização, isolamento e auditoria.
- `migrations.md`: inventário das migrations imutáveis V1–V7.
