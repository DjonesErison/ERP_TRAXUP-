# TRAXUP — Documento Mestre do Produto

> Documento vivo consolidado a partir das decisões funcionais, técnicas e visuais do projeto. Atualizado em setembro de 2026. Regra de acompanhamento: **especificado ≠ aprovado visualmente ≠ implementado ≠ testado ≠ publicado**. A Central TRAXUP usa backlog versionado, pesos e evidências para acompanhamento; seus percentuais representam progresso de projeto, não uma medição automática de completude de código.

## 1. Visão do produto

O TRAXUP é um ecossistema SaaS modular de gestão empresarial, multi-tenant, multiempresa e multifilial. O núcleo é o ERP Web conectado ao PDV, estoque, compras, vendas, financeiro, fiscal, CRM, BI, relatórios, aplicativos, administração SaaS, contabilidade e integrações externas.

Objetivo: integrar a jornada **compra → estoque → venda → recebimento → documento fiscal → financeiro → relacionamento → análise → contabilidade**, evitando ilhas de informação e retrabalho.

## 2. Princípios do produto

- Modularidade: o cliente ativa módulos conforme sua operação cresce.
- Multi-tenant: isolamento obrigatório dos dados de cada cliente SaaS.
- Multiempresa/multifilial: empresa, filial e permissões fazem parte do contexto operacional.
- Operação offline no PDV: venda continua mesmo sem conexão e sincroniza depois.
- Integração entre módulos: uma operação deve gerar automaticamente seus efeitos relacionados.
- Segurança e rastreabilidade: JWT, RBAC, auditoria, idempotência e LGPD são requisitos transversais.
- Simplicidade operacional: esconder complexidade técnica/fiscal do usuário sempre que possível.
- Evidência antes de status: documentação ou tela aprovada não significa funcionalidade implementada.

## 3. Tecnologia e arquitetura

| Camada | Tecnologia / decisão |
|---|---|
| Frontend Web | Angular 19 |
| Backend | Java + Spring Boot |
| API | REST |
| Contratos/API | OpenAPI / Swagger |
| Banco central | PostgreSQL |
| Banco local PDV | SQLite por terminal |
| Autenticação | JWT + refresh token |
| Autorização | RBAC / permissões |
| Migrations | Flyway |
| Mensageria planejada | Redis + Apache Kafka |
| Arquivos fiscais | Storage compatível com S3 |
| Containers | Docker |
| CI/CD da Central | GitHub Actions |
| Proxy/HTTPS | Caddy/Nginx |
| Infraestrutura | VPS Linux |

Redis/Kafka permanecem arquitetura definida, mas devem ser tratados como **especificados** até existir evidência concreta de implementação equivalente à utilizada para os demais componentes.

### Arquitetura lógica

ERP Web, PDV e aplicativos consomem serviços TRAXUP na nuvem. O backend aplica regras, persiste dados e integra os domínios. PostgreSQL guarda dados centrais; SQLite garante operação local do PDV; S3 compatível armazena artefatos fiscais. Integrações externas ficam atrás de contratos/adaptadores para reduzir acoplamento com fornecedores.

## 4. Jornada do cliente

Prospecção → cadastro → trial de 7 dias → criação da empresa → seleção de empresa/filial → onboarding → dados empresariais → configuração fiscal → usuários/permissões → clientes/produtos/fornecedores → entrada de NF de compra → estoque → configuração do PDV → homologação fiscal → primeira venda → pagamento → documento fiscal → estoque/financeiro/CRM/BI → contabilidade → suporte → renovação.

## 5. Jornada do produto

Compra → importação da NF → identificação de produto/NCM/tributação → atualização de custo → entrada no estoque → preço → disponibilização ERP/PDV/apps → venda → pagamento → documento fiscal → baixa de estoque → financeiro → margem/lucro → CRM/BI/relatórios → reposição.

## 6. Áreas do ecossistema

| Área | Escopo principal |
|---|---|
| ERP Web | Administração central da empresa |
| Clientes | Cadastro, Visão 360º, crédito e relacionamento |
| Produtos | Cadastro, preços, estoque, fiscal, grades e combos |
| Estoque/Compras | NF de entrada, custo, inventário, transferências e movimentações |
| Vendas | Pedidos/vendas, itens, pagamentos e vendedores |
| PDV | Venda, caixa, atendimento, offline e sincronização |
| Financeiro | Pagar/receber, caixa, bancos, cartões, PIX, DRE e conciliação |
| Fiscal | NF-e/NFC-e, perfil fiscal, XML, DANFE, rejeições e contingência |
| CRM | Campanhas, fidelização e retorno de clientes |
| BI/Relatórios | Indicadores, análises e relatórios dinâmicos |
| Apps | Vendedor, cliente e inventário móvel |
| Contabilidade | XML, SPED, inventário, Livro Caixa e fechamento mensal |
| SaaS Admin | Planos, assinaturas, cobrança, trial, bloqueio e suporte |
| Integrações | iFood, marketplaces, TEF, PSPs, bancos e parceiros |
| IA | Assistência operacional e análise proativa futura |

## 7. Dashboard do ERP

Deve apresentar vendas do dia/mês, ticket médio, produtos vendidos, clientes atendidos, contas a pagar/receber, saldos, estoque baixo, produtos sem giro, margem, metas e desempenho por filial, vendedor, produto e meio de pagamento.

## 8. Clientes

Cadastro com CPF/CNPJ, identificação, contato, endereço, dados comerciais/fiscais, limite, tabela de preço, consentimentos LGPD e dados de CRM. A Visão 360º reúne compras, produtos, valores, ticket médio, última compra, contas, créditos, campanhas, atendimentos e histórico. Está previsto autopreenchimento quando tecnicamente e legalmente suportado.

## 9. Produtos

Cadastro dividido em dados básicos, preços, estoque, fiscal e grades. Suporta custo, preço de venda, promoção, margem, preço mínimo, preços por filial/tabela/período, estoque mínimo/máximo, depósitos, NCM, CEST, CFOP, impostos, código de barras e variações.

### Produto Combo

Combos podem ser fixos ou permitir escolhas e adicionais, promoções e validade. A baixa de estoque ocorre pelos componentes, respeitando disponibilidade.

## 10. Estoque e compras

Entrada por compra/NF, ajuste, transferência, inventário móvel, venda, devolução, perda e reserva. A entrada de NF deve identificar fornecedor, produtos, quantidades, custo, NCM e tributação e, após conferência, atualizar estoque, custo, financeiro, fornecedor e fiscal. O onboarding de compra busca reduzir cadastros fiscais manuais e automatizar regras e devoluções.

## 11. Vendas e PDV

Fluxo principal: abrir caixa → vendedor → produtos → quantidade/desconto → cliente/CPF → pagamento → documento fiscal → impressão → conclusão.

Funcionalidades definidas: últimas vendas/consulta detalhada, reimpressão, segunda via, cancelamento com senha/permissão e justificativa, alteração de pagamento conforme regra, fechamento de caixa, mesas, delivery manual/integrado, impressão por destino, cozinha/caixa e autodetecção de impressoras no Windows.

### Operação offline

Cada terminal mantém SQLite local. Se a comunicação cair, o PDV continua vendendo, persiste eventos e sincroniza quando a conexão retorna. Cada terminal deve possuir identificação/série apropriada e a sincronização deve ser idempotente para impedir duplicidade.

### Segunda tela — PDV-SEG-001

Exibe itens, quantidades, preços, total, CPF, QR Code PIX, status, avaliação e ofertas, inclusive em cenários previstos de operação offline.

### Venda com mínima intervenção

Roadmap inclui self-checkout, POS integrado, celular do vendedor, Tap to Pay quando suportado e PDV tradicional.

### PDV Demo

Versão de demonstração com aproximadamente 10–20 produtos, simulação de venda e sincronização com o ERP para apoiar trial/prospecção.

## 12. Fiscal

O Fiscal é um domínio isolado para estabilidade e segurança, embora integrado a vendas, compras, produtos e financeiro.

Fluxo-alvo: venda → contrato fiscal → perfil da filial → validação → numeração → XML → assinatura → transmissão → SEFAZ → autorização/rejeição → XML processado/DANFE → armazenamento → contabilidade.

Escopo definido: NF-e, NFC-e, perfil fiscal por filial, numeração fiscal, certificado/CSC, homologação, XML, DANFE, rejeições, correção/reprocessamento, cancelamento, devolução, contingência e retomada.

### Situação e regra de evidência fiscal

Existe implementação concreta de perfil fiscal por filial, validação, numeração atômica, contrato Venda→Fiscal, estruturas de XML/tentativas/rejeições, correção e armazenamento fiscal. Parte da cadeia de assinatura/transmissão/processamento identificada no projeto opera em **homologação simulada**. Portanto, homologação simulada **não equivale a autorização fiscal real da SEFAZ**. Integração real SEFAZ, DANFE e operação de produção só podem ser consideradas entregues quando houver evidência específica.

### Repositório fiscal

Objetivo de retenção mínima de cinco anos, armazenamento S3 compatível, validação por hash, proteção contra sobrescrita divergente, download seguro, auditoria e acesso controlado da contabilidade.

## 13. Financeiro

Contas a pagar/receber, caixa, bancos, cartões, PIX, boletos, fluxo de caixa, DRE e conciliação. Uma venda deve alimentar automaticamente o financeiro conforme as formas de pagamento.

### DRE

Faturamento − impostos − custo das mercadorias = margem; margem − despesas = resultado operacional.

### FIN-CONC — Conciliação de cartões

Cruza vendas TRAXUP com adquirentes/PSPs, identificando conciliado, pendente, divergente, taxas, antecipações, cancelamentos, estornos e chargebacks. Prevê integração TEF/Payment Hub e importação de dados/arquivos de adquirentes e PSPs.

### FIN-BANK — Integração bancária

Domínio separado da conciliação de cartões. Arquitetura baseada em contrato único e adaptadores por banco/hub/PSP, evitando acoplamento do Financeiro a fornecedores específicos.

Escopo definido: cadastro de contas bancárias; saldo e extrato; importação/sincronização de movimentações; conciliação bancária automática; PIX; PIX Cobrança com QR Code dinâmico e TXID; baixa automática por webhook; boletos; transferências/pagamentos em evolução; PIX Automático no roadmap; Open Finance/multibanco em fase futura.

Fluxo esperado de cobrança: título/venda → geração da cobrança → banco/PSP → pagamento → webhook → identificação idempotente → baixa do título → movimento bancário → conciliação.

Credenciais bancárias, certificados, client secrets, tokens e chaves privadas não devem ser tratados como configuração comum do frontend/banco; devem permanecer protegidos no backend/cofre de segredos. Webhooks exigem validação, idempotência, auditoria e proteção contra processamento duplicado. O contexto bancário segue tenant → empresa → filial → conta bancária → provedor.

## 14. CRM e fidelização

Segmentação de clientes novos, frequentes, VIP, inativos, aniversariantes e maior ticket; campanhas, histórico e ações automáticas de retorno após período sem compra.

## 15. BI e relatórios

Indicadores de produtos mais vendidos/lucrativos, filiais/vendedores, clientes de maior valor, estoque parado, horários, meios de pagamento, metas e margens. Relatórios padrão e relatórios dinâmicos com seleção livre de campos.

## 16. Aplicativos

### App do vendedor

Vendas, metas, comissão, clientes, produtos, promoções e novidades; evolução para venda móvel e recursos de pagamento quando suportados.

### App do cliente

Cadastro, histórico, promoções, cupons, pedidos, fidelidade, contato, pagamentos e serviços.

### Inventário móvel

Uso de celular/tablet para contagem e conferência de estoque.

## 17. Contabilidade

Área de mínimo privilégio, sem conceder poderes operacionais desnecessários. O escopo está decomposto em entregas rastreáveis:

- Repositório fiscal e exportação de XML, com retenção, storage, download seguro e manifesto.
- Acesso dedicado da contabilidade, isolado por tenant/filial.
- Livro Caixa somente leitura, com paginação e consolidação.
- Consulta de inventário pela contabilidade.
- SPED com fila, processamento assíncrono, reprocessamento, diagnóstico e gate de homologação; evolução por filial ainda deve ser comprovada antes de considerar completa.
- Fechamento mensal contábil consolidando XML, SPED, Livro Caixa e inventário.

## 18. Administração SaaS, trial e suporte

Cadastro de clientes assinantes, planos e funcionalidades, situação, mensalidades, vencimentos/pagamentos, inadimplência, histórico financeiro, boleto, bloqueio/reativação e notificações. Prevê acesso do contador, suporte interno, autoatendimento/segunda via, acesso remoto futuro e comercialização de certificado digital.

White-label/prospecção: cadastro da empresa/contato e trial de 7 dias com ERP liberado. PDV Demo complementa a experiência de teste.

## 19. Notificações

Certificado próximo do vencimento, boleto vencido, estoque baixo, produto sem giro, cliente inativo, conta vencendo, rejeição fiscal, sincronização de PDV parada e outros eventos configuráveis.

## 20. Dados e banco

### Bancos utilizados

- **PostgreSQL:** banco central SaaS/multi-tenant.
- **SQLite:** banco local por terminal do PDV para continuidade offline.
- **Flyway:** versionamento das migrations do PostgreSQL.

### Regra documental para tabelas

A documentação distingue **entidade planejada** de **tabela fisicamente comprovada por migration/código**. Uma entidade funcional não deve ser registrada como tabela criada sem evidência técnica.

### Grupos lógicos do modelo

| Domínio | Entidades / estruturas previstas ou implementadas conforme evidência |
|---|---|
| Empresa | empresas, filiais, usuários, vínculo usuário-filial |
| Segurança | perfis, permissões, refresh tokens, auditoria |
| Clientes | clientes, endereços, contatos, histórico, CRM |
| Produtos | produtos, grades, preços, estoque, fiscal, combos |
| Estoque | saldos, depósitos, movimentos, inventários, transferências |
| Compras | fornecedores, compras, itens, entradas fiscais |
| Vendas | vendas, itens, pagamentos, vendedores, caixas, cancelamentos |
| Financeiro | pagar, receber, lançamentos, contas bancárias, cartões, conciliação |
| Fiscal | documentos, itens, perfil fiscal, numeração, tentativas, XML, eventos, rejeições |
| Contabilidade | exportações XML, Livro Caixa, inventário, filas/processamento SPED |
| SaaS | assinantes, planos, mensalidades, módulos e trial |
| Integrações | provedores, webhooks, eventos, sincronizações e logs |

Um inventário físico completo deve ser gerado a partir das migrations do backend, contendo tabela, finalidade, migration de origem, campos-chave, FKs, tenant/filial e módulo. Enquanto a árvore consultada da Central não expuser essas migrations, o Documento Mestre não inventará nomes físicos.

## 21. Multiempresa, segurança e LGPD

O tenant deve ser derivado do contexto autenticado, não confiado a cabeçalhos arbitrários do cliente. Permissões definem o que caixa, gerente, financeiro, fiscal, contador e administrador podem consultar/executar. JWT/refresh, rotação/revogação, RBAC, auditoria, integridade multi-tenant e LGPD são requisitos transversais.

## 22. Integrações externas

SEFAZ, iFood/delivery, marketplaces, TEF/Payment Hub, adquirentes/PSPs, bancos/Open Finance, PIX, certificados e armazenamento compatível com S3. Integrações devem preferir portas/adaptadores, contratos versionados, idempotência, observabilidade e tratamento seguro de credenciais.

## 23. Central TRAXUP

A Central é a fonte de acompanhamento do produto e não deve ser confundida com o ERP funcional. Ela reúne Dashboard, Ecossistema, Módulos, Funcionalidades, Regras de Negócio, Dependências, Testes, Documentação, Roadmap, Histórico, GitHub, Referências Visuais e Configurações.

O acompanhamento usa backlog versionado, pesos de escopo e evidências (PR, commit, CI, deploy e documento). A decomposição de um item amplo pode compartilhar o peso original para não aumentar artificialmente o progresso apenas por criar mais linhas.

### Estados e interpretação

A Central atualmente acompanha estados como Concluída, Em implementação, Especificada e Planejada, mas a documentação adota a distinção conceitual mais rigorosa: **especificado → aprovado visualmente (quando aplicável) → implementado → testado → publicado/entregue**. Uma evidência pode aumentar rastreabilidade sem aumentar o percentual quando o estado funcional não mudou.

## 24. Fluxo visual

Proposta visual → revisão visual/funcional → aprovação → organização da referência no GitHub → implementação Angular → integração backend → testes → publicação. Imagem aprovada é referência de interface, não prova de funcionalidade pronta.

O catálogo visual está versionado em `docs/design/`; a sequência aprovada alcança UI-024, incluindo login, recuperação de senha, seleção empresa/filial, onboarding, usuários/permissões, cliente, Visão 360º, produto e preços. Próximas telas seguem UI-025 em diante.

## 25. Infraestrutura e publicação

A Central Web é containerizada em Docker, publicada por imagem GHCR e servida em `central.traxup.com.br`. O fluxo de publicação usa GitHub Actions com CI, build Docker e deploy, incluindo health checks e rollback. Builds possuem metadados de commit para rastreabilidade. Alterações são consideradas publicadas somente após validação do SHA exato em CI → Docker → Deploy.

A implementação funcional do ERP/backend permanece separada do trabalho de acompanhamento/documentação da Central.

## 26. Roadmap funcional de alto nível

1. Fundação, infraestrutura, segurança e multi-tenant.
2. Empresa, filiais, usuários e permissões.
3. Clientes, fornecedores e produtos.
4. Estoque, compras e inventário.
5. Fiscal e homologação real.
6. Vendas, PDV offline e sincronização.
7. Financeiro, cartões e integração bancária.
8. Contabilidade e SPED.
9. CRM, relatórios e BI.
10. Apps, delivery, marketplaces e integrações.
11. Administração SaaS, trial, suporte e automações.
12. Evolução de IA.

Frentes podem avançar em paralelo quando as dependências permitirem.

## 27. IA — visão futura

Assistente capaz de responder perguntas de negócio, detectar queda de venda, filiais abaixo da meta, contas a receber, clientes inativos, risco de falta de estoque e outras anomalias, tornando o ERP mais proativo. IA deve apoiar decisões, sem substituir regras transacionais determinísticas.

## 28. Critério de verdade do projeto

Para documentação e Central:

- **Planejado:** intenção aprovada para roadmap.
- **Especificado:** regra/escopo documentado.
- **Aprovado visualmente:** interface de referência aprovada.
- **Em implementação:** existe trabalho técnico parcial rastreável.
- **Implementado:** código correspondente ao escopo existe.
- **Testado:** comportamento foi validado por testes adequados.
- **Publicado/Entregue:** artefato correspondente passou pelo fluxo de publicação/implantação aplicável.

Nenhum desses estados deve ser inferido apenas de uma imagem, documento, percentual ou PR parcial.

## 29. Objetivo final

Entregar um ecossistema único no qual compra, estoque, venda, recebimento, fiscal, financeiro, relacionamento, análise e contabilidade estejam conectados, com operação offline onde necessário, administração SaaS, segurança multi-tenant e integrações extensíveis, permitindo que o TRAXUP cresça por módulos sem perder consistência operacional.