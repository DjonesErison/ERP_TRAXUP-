# Fase 4 - Metricas de saude das integracoes financeiras

Este incremento amplia a observabilidade operacional sem depender de um banco ou PSP concreto.

## Metricas

O resumo de saude de cada integracao passa a expor, considerando no maximo as 50 tentativas mais recentes do mesmo tenant e da mesma integracao:

- quantidade de sucessos;
- quantidade de falhas;
- duracao media das tentativas em milissegundos;
- falhas consecutivas;
- ultima tentativa e ultimo sucesso.

Quando ainda nao houve execucao, sucessos e falhas ficam em zero e a duracao media permanece nula, evitando inventar dados operacionais.

## Seguranca e isolamento

A integracao continua sendo validada por `id + tenant` antes da leitura das tentativas. O incremento e somente leitura sobre dados de observabilidade ja persistidos, nao cria nova permissao, nao altera auditoria de mutacao e nao expoe checkpoint, mensagem de excecao, token, chave, certificado ou qualquer credencial.

O endpoint de saude continua protegido por `FINANCEIRO_CONCILIACAO_LER`.

A TRAXUP Central permanece separada do runtime do TPlug ERP.
