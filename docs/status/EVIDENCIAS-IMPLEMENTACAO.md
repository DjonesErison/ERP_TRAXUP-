# TRAXUP — Evidências auditadas de implementação

Este registro complementa o status automático da Central. Uma evidência comprova uma entrega concreta, mas não altera sozinha o percentual do projeto nem autoriza declarar como concluído um escopo mais amplo.

## Fundação técnica

### ARC-MOD — Arquitetura modular e multi-tenant
- PR #7 — documentação técnica da Fase 0 consolidando autenticação, multi-tenancy, RBAC, auditoria e migrations V1–V7.
- PR #2 — tenant passa a ser derivado do JWT autenticado e as APIs deixam de confiar em `X-Tenant-Id` como fonte de autoridade.
- PR #287 — vínculo usuário-filial com integridade multi-tenant e RBAC de gerenciamento.

Conclusão auditada: existem evidências concretas de isolamento multi-tenant e controles estruturais, mas o item permanece `Em implementação` porque representa a arquitetura do ecossistema inteiro, não apenas a fundação inicial.

### API-REST — API REST e contratos
- PR #12 — OpenAPI/Swagger integrado ao backend, JWT Bearer documentado e teste de integração do documento OpenAPI.
- PR #171 — frontend Angular 19 consumindo APIs REST reais com tenant derivado do JWT.
- PR #173 — login, refresh e logout conectados ao contrato real de autenticação do backend.

Conclusão auditada: REST/OpenAPI possui implementação verificável. O item permanece `Em implementação` porque também cobre a integração completa entre frontend, PDV e serviços, ainda não comprovada como concluída.

### AUTH-001 — Autenticação e autorização
- PR #2 — JWT HS256, login por tenant/e-mail/senha, refresh token opaco armazenado como SHA-256, rotação, revogação no logout e testes de isolamento.
- PR #8 — permissões de usuário e proteção por `@PreAuthorize`, incluindo teste HTTP 403.
- PR #173 — sessão web Angular com JWT, refresh compartilhado e logout.
- PR #287 — evolução do RBAC para escopo explícito por filial.

Conclusão auditada: autenticação e RBAC têm implementação real e ampla. O status permanece `Em implementação` para não declarar concluído todo o escopo de autorização do produto enquanto os módulos continuam evoluindo.

### DB-PG — PostgreSQL multi-tenant
- PR #7 — inventário inicial das migrations V1–V7 e isolamento por tenant.
- PR #87 — testes de integração reais em PostgreSQL provando isolamento de títulos financeiros entre tenants.
- PR #105 — integridade tenant-safe da tesouraria com testes cross-tenant/cross-filial.
- PR #106 — integridade `tenant_id + usuario_id` nos títulos financeiros com testes PostgreSQL.
- PR #287 — migration V101 com chaves estrangeiras compostas por tenant para usuário-filial.

Conclusão auditada: PostgreSQL, Flyway e invariantes multi-tenant estão efetivamente presentes. O item permanece `Em implementação` porque o schema continua evoluindo com o produto.

### MSG-001 — Redis e Kafka
A busca por PRs com Redis/Kafka não localizou implementação concreta equivalente. A única ocorrência relevante encontrada foi o próprio planejamento/status da Central.

Conclusão auditada: manter `Especificada`, sem evidência de implementação vinculada.

### DOCKER-001 — Containers e publicação
- PR #13 — Dockerfile multi-stage do backend com Java 21, usuário não-root, configuração por variáveis de ambiente e validação de `docker build` no CI.
- A Central já possui evidência própria de imagem GHCR, health checks e deploy automático.

Conclusão auditada: mantém `Concluída` para o escopo rastreado de containers/publicação, agora com evidência adicional do backend.

## Fiscal

### FIS-PERFIL — Perfil Fiscal por Filial
- PR #205 — migration V76 e persistência do perfil fiscal por tenant/filial.
- PR #206 — API GET/PUT, validações fiscais, RBAC, auditoria e testes.
- PR #207 — validação do perfil da filial na prontidão do documento fiscal.
- PR #208 — migration V77 e base segura de numeração fiscal.
- PR #209 — reserva transacional, atômica e idempotente do número fiscal.

Conclusão auditada: há evidência suficiente para manter `FIS-PERFIL` como Concluída. As PRs #207–#209 também comprovam integração parcial do perfil com a preparação da emissão, sem equivaler ao fluxo SEFAZ completo.

### Repositório fiscal / Contabilidade
- PR #248 — metadados, fila e retenção mínima de cinco anos.
- PR #250 — arquivamento real em storage S3 compatível com SHA-256.
- PR #251 — agendamento do arquivamento.
- PR #252 — download seguro de XML com validação de integridade.
- PR #253 — consulta tenant-safe do repositório.
- PR #254 — exportação de XMLs para a contabilidade.
- PR #255 — manifesto CSV no pacote contábil.
- PR #257 — acesso dedicado da contabilidade com privilégio mínimo.
- PR #259 — provisionamento do perfil CONTABILIDADE.
- PR #262 — consulta de inventário para a contabilidade sem poderes operacionais de estoque.
- PR #269 — reprocessamento controlado de exportações SPED.
- PR #274 — resumo operacional da fila SPED.
- PR #277 — gate de homologação explícita do gerador SPED.
- PR #280 — proteção no banco contra exclusão antecipada de XML/DANFE.
- PR #281 — proteção contra sobrescrita divergente no storage.
- PR #282 — exportação contábil pela data fiscal.
- PR #283 — paginação determinística da exportação contábil.

Conclusão auditada: o item amplo `CONT-PORTAL` não deve permanecer tratado como uma única entrega puramente planejada. Existem subentregas concretas de repositório fiscal, acesso contábil, inventário e SPED. A interface completa do Portal da Contabilidade e itens como livro caixa ainda precisam ser avaliados separadamente.

## PDV

A auditoria por PR não localizou evidência equivalente para `PDV-OFF` ou `PDV-SYNC` usando os termos SQLite/offline/sincronização. Esses itens permanecem com o estado rastreado atual, mas sem evidência vinculada até localizar implementação verificável.

## Regra de auditoria

1. Especificação visual ou documento funcional não equivale a implementação.
2. PR de infraestrutura parcial não conclui automaticamente um módulo inteiro.
3. Status `Concluída` exige evidência compatível com o escopo declarado do item.
4. Quando um item amplo mistura partes entregues e pendentes, ele deve ser decomposto em itens menores antes de recalcular o progresso.
5. Evidência adicional pode aumentar a cobertura de rastreabilidade sem necessariamente aumentar o percentual quando o status auditado permanece o mesmo.
