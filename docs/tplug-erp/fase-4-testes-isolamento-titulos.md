# Fase 4 - Testes de isolamento dos titulos financeiros

Este incremento fortalece a cobertura de contas a receber e contas a pagar com testes de integracao executados contra o PostgreSQL usado no CI.

Os testes inserem dados de tenants distintos e validam que filtros e resumos retornam apenas registros do par correto de tenant, filial e contraparte (cliente ou fornecedor). Tambem validam os totais agregados de valor original, valor liquidado, saldo ativo e contagens por estado.

Nao ha mudanca de contrato de API, regra de negocio, RBAC, auditoria, migration ou runtime. A TRAXUP Central permanece separada do TPlug ERP.
