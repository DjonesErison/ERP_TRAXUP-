# Fase 4 - Resumo do painel operacional

O painel operacional por conta passa a oferecer um resumo agregado dos estados de saude das integracoes financeiras, sem depender de provedor concreto.

- `GET /api/v1/financeiro/integracoes/contas/{contaId}/painel/resumo` exige `FINANCEIRO_CONCILIACAO_LER`.
- A agregacao reutiliza exatamente a listagem tenant-scoped da conta e o resumo de saude de cada integracao.
- O retorno informa `total`, `saudaveis`, `atencao` e `semExecucao`.
- O endpoint e somente leitura, nao gera nova auditoria de mutacao e nao expoe checkpoint, mensagens de erro ou segredos.
- Nenhum adapter concreto ou credencial foi adicionado.
- A TRAXUP Central permanece fora do runtime do TPlug ERP.
