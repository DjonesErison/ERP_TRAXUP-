# Fase 4 - Painel operacional de integracoes financeiras

Este incremento consolida, por conta financeira e tenant, as integracoes configuradas e o respectivo resumo de saude operacional sem depender de banco, adquirente ou PSP concreto.

## Endpoint

- `GET /api/v1/financeiro/integracoes/contas/{contaId}/painel`
- exige `FINANCEIRO_CONCILIACAO_LER`;
- valida a conta pelo tenant corrente reutilizando o fluxo de listagem de integracoes;
- retorna somente integracoes pertencentes a conta e tenant informados pelo contexto autenticado;
- para cada integracao, agrega o resumo de saude ja existente (`SEM_EXECUCAO`, `SAUDAVEL` ou `ATENCAO`).

## Isolamento e seguranca

- tenant nunca e aceito por parametro do cliente;
- a lista base vem de `tenant_id + conta_financeira_id`;
- cada resumo de saude continua validando `integracao_id + tenant_id` antes de acessar tentativas;
- o endpoint e somente leitura e nao cria nova auditoria de mutacao;
- nenhum token, chave, certificado, senha, checkpoint bruto ou mensagem de erro e exposto;
- nenhuma regra especifica de provedor foi adicionada.

## Testes

Os testes cobrem a consolidacao das integracoes retornadas no escopo da conta/tenant e garantem que um painel vazio nao dispara consultas de saude desnecessarias.

A TRAXUP Central permanece separada do runtime do TPlug ERP e nao e alterada por este incremento.
