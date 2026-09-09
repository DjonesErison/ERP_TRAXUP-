# Fase 7 — Contrato Venda → Fiscal

Esta entrega cria a primeira fronteira persistida do módulo fiscal do TPlug ERP. O objetivo é separar o núcleo comercial/PDV da futura integração com SEFAZ, sem antecipar regras tributárias, certificados ou transmissão.

## Regras implementadas

- somente `PedidoVenda` em estado `FATURADO` pode originar solicitação fiscal;
- tenant é sempre derivado do contexto autenticado;
- filial é derivada da própria venda faturada;
- modelos aceitos nesta fundação: `NFCE` e `NFE`;
- ambientes aceitos: `HOMOLOGACAO` e `PRODUCAO`;
- uma mesma venda/modelo/ambiente possui identidade única e replay idempotente;
- primeira criação inicia em `PENDENTE`;
- estados reservados para evolução do processador: `PROCESSANDO`, `AUTORIZADO`, `REJEITADO`, `CONTINGENCIA` e `CANCELADO`;
- criação é auditada sem certificado, token, XML ou segredo fiscal.

## API

Base: `/api/v1/fiscal/solicitacoes`

- `GET /api/v1/fiscal/solicitacoes` — requer `FISCAL_DOCUMENTO_LER`;
- `GET /api/v1/fiscal/solicitacoes/{id}` — requer `FISCAL_DOCUMENTO_LER`;
- `POST /api/v1/fiscal/solicitacoes` — requer `FISCAL_DOCUMENTO_EMITIR`;
- `POST /api/v1/fiscal/solicitacoes/{id}/processamento` — inicia `PENDENTE → PROCESSANDO`, requer `FISCAL_DOCUMENTO_EMITIR` e aceita replay idempotente.

O `POST` de criação retorna HTTP `201` na primeira chamada e HTTP `200` em replay idêntico.
O início do processamento é auditado apenas na primeira transição; chamadas repetidas em `PROCESSANDO` não duplicam auditoria.

## Fora do escopo desta fatia

- geração de XML;
- assinatura com certificado digital;
- comunicação com SEFAZ;
- numeração/chave de acesso/protocolo;
- DANFE;
- armazenamento S3;
- contingência operacional;
- correção de rejeições;
- cancelamento fiscal autorizado;
- produto de homologação `Bola`.

Esses itens serão construídos sobre esta fronteira, sem levar regras fiscais ao frontend ou ao núcleo do PDV.

A TRAXUP Central permanece sem alteração de runtime.
