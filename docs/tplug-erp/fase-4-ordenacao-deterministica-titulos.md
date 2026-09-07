# Fase 4 - Ordenacao deterministica dos titulos financeiros

Este incremento fortalece as listagens operacionais de contas a receber e contas a pagar sem alterar o contrato funcional dos endpoints.

## Regra

- as consultas filtradas continuam ordenando primeiro por vencimento crescente;
- em seguida, preservam a prioridade de criacao mais recente;
- quando vencimento e instante de criacao empatam, o UUID do titulo passa a ser o ultimo criterio de desempate;
- o objetivo e produzir ordem estavel para consumidores atuais e preparar o caminho para paginacao futura sem oscilar registros entre leituras equivalentes.

## Seguranca e arquitetura

- `tenant_id` continua obrigatorio em todas as consultas;
- RBAC permanece inalterado;
- nao existe mutacao adicional, portanto nao ha nova auditoria de escrita;
- nao ha migration nem mudanca de regra financeira;
- nenhum arquivo de runtime da TRAXUP Central e alterado.
