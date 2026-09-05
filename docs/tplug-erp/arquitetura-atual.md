# Arquitetura Atual do TPlug ERP

## Direção arquitetural

O TPlug ERP inicia como monólito modular. A prioridade é manter separação clara entre domínios, baixo acoplamento e possibilidade de evolução futura sem introduzir microserviços prematuramente.

Camadas de referência:

- API: controllers, requests e responses.
- Application: casos de uso e coordenação transacional.
- Domain: regras de negócio e contratos.
- Infrastructure: persistência, mensageria, armazenamento e integrações.

## Stack oficial

- Frontend: Angular 19.
- Backend: Java 21 + Spring Boot.
- Banco: PostgreSQL.
- API: REST com OpenAPI/Swagger.
- Autenticação: JWT + Refresh Token.
- Cache e locks: Redis.
- Eventos e mensageria: Apache Kafka.
- Arquivos: armazenamento S3 compatível.
- Deploy: Docker em Ubuntu Linux.
- CI/CD e fonte de verdade técnica: GitHub.

Redis, Kafka, armazenamento S3, OpenAPI e o frontend operacional do TPlug ERP ainda não fazem parte da implementação concluída nesta etapa.

## Multi-tenancy

Toda operação de negócio deve ser isolada por tenant. O identificador de tenant de requisições autenticadas vem do claim `tenant_id` do JWT. Cabeçalhos enviados pelo cliente não podem sobrescrever esse contexto.

As entidades de negócio devem ser consultadas por métodos tenant-scoped e, quando aplicável, o banco também deve impedir referências cruzadas entre tenants por constraints compostas.

## Segurança e autorização

O backend é stateless para autenticação HTTP. Access tokens são JWTs curtos e refresh tokens são rotativos. O RBAC utiliza catálogo global de permissões e perfis pertencentes ao tenant. As permissões efetivas são incorporadas ao JWT e convertidas em authorities do Spring Security.

## Auditoria

A auditoria registra eventos append-only com tenant, usuário autenticado quando disponível, empresa, filial, operação, entidade, identificador afetado, detalhes relevantes e timestamp UTC. Operações críticas de criação e desativação em Empresa, Filial e Usuário já são instrumentadas.

## Princípios obrigatórios

- O frontend não decide regras fiscais ou regras críticas de negócio.
- Operações sensíveis devem ser idempotentes quando houver risco de repetição.
- Segredos nunca são persistidos na documentação ou no código.
- Migrations Flyway aplicadas são imutáveis; alterações futuras entram em novas versões.
- Código, testes e documentação fazem parte da mesma entrega técnica.
