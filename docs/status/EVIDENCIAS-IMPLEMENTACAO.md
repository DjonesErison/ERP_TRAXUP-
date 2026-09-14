# TRAXUP — Evidências auditadas de implementação

Este registro complementa o status automático da Central. Uma evidência comprova uma entrega concreta, mas não altera sozinha o percentual do projeto nem autoriza declarar como concluído um escopo mais amplo.

## Fundação técnica

### ARC-MOD / AUTH-001 / DB-PG
- PR #7 — documentação técnica da Fase 0 consolidando autenticação, multi-tenancy, RBAC, auditoria e migrations V1–V7.
- Uso no status: evidência histórica da fundação. Não comprova que toda a arquitetura, segurança ou banco estejam concluídos.

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
