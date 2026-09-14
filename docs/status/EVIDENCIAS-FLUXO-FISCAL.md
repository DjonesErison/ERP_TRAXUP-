# Evidências do fluxo fiscal — Central TRAXUP

Data da auditoria: 2026-09-14

## Regra de leitura

Esta auditoria serve exclusivamente para o acompanhamento da Central. Ela não altera o backend fiscal. A Central deve distinguir infraestrutura/fluxo simulado de integração real com a SEFAZ.

**Regra principal:** homologação simulada não equivale a autorização fiscal real da SEFAZ.

## FIS-EMISSAO — Em implementação

Há evidência concreta de integração progressiva do perfil fiscal com o fluxo de emissão:

- PR #194 — contrato persistido Venda → Fiscal, com NF-e/NFC-e e estados do processador; SEFAZ, XML, assinatura, contingência efetiva e rejeições ficaram fora desse primeiro corte.
- PR #198 — XML preparatório em homologação, explicitamente não declarado compatível com o leiaute oficial NF-e/NFC-e.
- PR #205/#206/#207 — perfil fiscal por filial, API e validação do perfil na prontidão.
- PR #209 — numeração fiscal atômica.
- PR #216/#217 — estrutura e assinatura simulada em homologação.

Conclusão: manter `Em implementação`.

## FIS-SEFAZ — Em implementação, sem autorização real comprovada

Existe infraestrutura desacoplada para transmissão e um fluxo determinístico de homologação simulada:

- PR #218 — schema de transmissão simulada, ambiente HOMOLOGACAO, provedor SIMULADO e status AUTORIZADO_SIMULADO.
- PR #219 — porta para futuros provedores/SEFAZ e adaptador simulado; não realiza comunicação externa e não declara autorização da SEFAZ.
- PR #220/#221 — XML processado simulado; artefato TraxUP sem validade fiscal.
- PR #222 — histórico consolidado do fluxo, preservando explicitamente a semântica SIMULADO.

Conclusão: manter `Em implementação`. Não promover para concluída enquanto não houver evidência de comunicação real, schemas oficiais aplicáveis, autorização/protocolo real e tratamento operacional de produção.

## FIS-CONT — Especificada

A PR #194 criou o estado `CONTINGENCIA`, mas declarou a contingência efetiva fora do escopo. A auditoria não encontrou implementação equivalente que justifique promoção.

Conclusão: manter `Especificada`.

## FIS-REJ — Em implementação

Há implementação backend concreta do ciclo de rejeição e correção:

- PR #223 — estrutura tenant-safe para rejeições.
- PR #224 — API de rejeições, histórico e início de correção.
- PR #227/#228 — novas tentativas rastreáveis após correção, preservando artefatos anteriores.
- PR #229 — vínculo de XML, assinatura, transmissão e processado às tentativas.
- PR #234 — assinatura do XML corrigido por tentativa.
- PR #235 — transmissão simulada da tentativa corrigida em homologação.
- PR #236 — processado da tentativa corrigida.
- PR #238 — histórico consolidado das reemissões.

Isso comprova implementação backend relevante, mas não comprova por si só a tela operacional de correção imediata nem rejeição real retornada pela SEFAZ em produção.

Conclusão: promover no rastreamento da Central de `Especificada` para `Em implementação`, preservando a ressalva de que o fluxo externo continua simulado.

## Critério para próximos avanços

Nenhum item fiscal deve receber `Concluída` por possuir apenas schema, documentação, adaptador simulado ou estado de domínio. A evidência deve cobrir o escopo declarado do item. Para SEFAZ e contingência, exigir evidência operacional real antes da promoção.