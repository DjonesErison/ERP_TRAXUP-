# TRAXUP — Documento Mestre do Produto

> Documento funcional consolidado em linguagem de negócio. Complementa o catálogo visual e a documentação técnica. As porcentagens de implementação exibidas na Central são estimativas até existir fonte automatizada de status.

## Visão do produto

O TRAXUP é um ecossistema SaaS de gestão empresarial. O núcleo é o ERP Web, conectado ao PDV, estoque, compras, financeiro, fiscal, CRM, BI, relatórios, aplicativos, administração SaaS, contabilidade e integrações externas. O cliente pode começar com parte do ecossistema e ativar novos módulos conforme sua operação cresce.

## Jornada do cliente

Prospecção → cadastro → trial de 7 dias → criação da empresa → seleção de empresa/filial → onboarding → dados da empresa → configuração fiscal → usuários e permissões → produtos/clientes/fornecedores → entrada de NF de compra → estoque → configuração do PDV → homologação fiscal → primeira venda → pagamento → documento fiscal → estoque/financeiro/CRM/BI → contabilidade → suporte → renovação da assinatura.

Objetivo: reduzir configuração manual e permitir que o cliente entre no TRAXUP, configure a empresa e comece a operar com o máximo de orientação e automação possível.

## Jornada do produto

Compra do produto → importação da NF → identificação do item/NCM/tributação → atualização de custo → entrada no estoque → definição de preço → disponibilização no ERP/PDV/apps → venda → pagamento → documento fiscal → baixa de estoque → movimento financeiro → margem/lucro → CRM/BI/relatórios → reposição.

Produtos podem ter grades, códigos de barras e preços por variação. Combos podem baixar automaticamente seus componentes e permitir grupos de escolha e adicionais.

## Áreas do ecossistema

| Área | Papel para o cliente |
|---|---|
| ERP Web | Centro de administração da empresa |
| PDV | Venda, caixa e atendimento |
| Estoque/Compras | Produtos, entrada, inventário e movimentações |
| Financeiro | Contas, caixa, recebimentos, DRE e conciliação |
| Fiscal | NF-e, NFC-e, XML, DANFE, contingência e rejeições |
| CRM | Relacionamento, campanhas e retorno de clientes |
| BI/Relatórios | Indicadores, análises e relatórios dinâmicos |
| Apps | Vendedor, cliente e inventário móvel |
| SaaS Admin | Assinaturas, planos, cobrança, trial e bloqueio |
| Contabilidade | XML, SPED, inventário, livro caixa e documentos |
| Integrações | iFood, marketplaces, pagamentos, TEF, bancos e parceiros |

## Dashboard do ERP

O dashboard deve responder rapidamente como está a empresa: vendas do dia/mês, ticket médio, produtos vendidos, clientes atendidos, contas a pagar/receber, saldo, estoque baixo, produtos sem giro, margem, meta, desempenho por filial/vendedor/produto e meios de pagamento.

## Clientes

Cadastro com identificação, CPF/CNPJ, contato, endereço, informações comerciais/fiscais, limite/tabela de preço, CRM e consentimentos LGPD. A Visão 360º deve reunir compras, produtos, valores, ticket médio, última compra, contas, créditos, campanhas, atendimentos e histórico.

## Produtos

Cadastro organizado por dados básicos, preços, estoque, fiscal e grades. Deve suportar custo, preço de venda, promoção, margem, preço mínimo, preços por filial/tabela/período, estoque mínimo/máximo, depósitos, NCM/CEST/CFOP/impostos, código de barras e variações.

## Estoque e compras

Operações: entrada por compra/NF, ajuste, transferência, inventário móvel, venda, devolução, perda e reserva. A entrada de NF deve identificar fornecedor/produtos/quantidades/custo/NCM/tributação e, após conferência, atualizar estoque, custo, financeiro, fornecedor e fiscal.

## PDV

Fluxo principal: abrir caixa → vendedor → produtos → quantidade/desconto → cliente/CPF → pagamento → documento fiscal → impressão → conclusão. Deve suportar últimas vendas, reimpressão, cancelamento com permissão, alteração de pagamento conforme perfil, mesas, delivery, impressão por destino e segunda tela do cliente.

### Operação offline

Cada terminal mantém SQLite local. Se a comunicação cair, o PDV continua vendendo, guarda os eventos e sincroniza quando a conexão retorna. Cada terminal utiliza série própria e a sincronização deve ser idempotente para evitar duplicidade.

### Segunda tela e autoatendimento

A segunda tela mostra itens, quantidades, preços, total, CPF, QR Code PIX, status, pesquisa de satisfação e ofertas. O ecossistema também prevê self-checkout, POS integrado, Tap to Pay/NFC e celular do vendedor como terminal quando suportado.

## Fiscal

Fluxo: venda → montagem do documento → validação → envio à SEFAZ → autorização ou rejeição. Em autorização, armazenar XML e disponibilizar DANFE; em rejeição, orientar correção. Deve suportar NF-e, NFC-e, cancelamento, devolução, contingência e retomada. O repositório fiscal é por cliente, com objetivo de retenção por cinco anos e acesso controlado da contabilidade.

O Assistente de Homologação verifica certificado, CSC, CNPJ, inscrição estadual, regime, NCM, CFOP, impostos, ambiente e comunicação com SEFAZ.

## Financeiro

Contas a pagar/receber, caixa, bancos, cartões, PIX, boletos, fluxo de caixa, DRE e conciliação. Uma venda deve alimentar automaticamente o financeiro conforme suas formas de pagamento.

### Conciliação de cartões

Cruza vendas do TRAXUP com adquirentes/PSPs e identifica conciliado, pendente, divergente, taxas, antecipações, cancelamentos, estornos e chargebacks.

### DRE

Faturamento − impostos − custo das mercadorias = margem; margem − despesas = resultado operacional.

## CRM e fidelização

Usa a base do ERP para identificar clientes novos, frequentes, VIP, inativos, aniversariantes e de maior ticket. Deve permitir campanhas e ações de retorno, inclusive a partir de períodos sem compra.

## BI e relatórios

O BI deve responder quais produtos vendem e lucram mais, filiais/vendedores com melhor desempenho, clientes de maior valor, estoque parado, horários de maior venda e meios de pagamento. Além dos relatórios padrão, o usuário poderá combinar campos para relatórios personalizados.

## Aplicativos

O app do vendedor acompanha vendas, metas, comissão, clientes, produtos, promoções e novidades. O app do cliente poderá reunir cadastro, histórico, promoções, cupons, pedidos, fidelidade, contato e pagamentos.

## Contabilidade

Área própria para XML, NF-e/NFC-e, SPED, inventário, livro caixa e demais documentos autorizados, reduzindo a dependência de envio manual pelo lojista.

## Administração SaaS, trial e suporte

O TRAXUP administra assinantes, planos, ativação, mensalidades, vencimentos, status, usuários, filiais, PDVs e módulos. Prevê cobrança/boleto, bloqueio e reativação, notificações e white-label. O trial libera o ERP por 7 dias e pode oferecer PDV Demo com produtos de exemplo e simulação de sincronização.

O suporte interno deve oferecer autoatendimento, segunda via de boleto, orientações de cadastro/configuração/fiscal e escalonamento para atendimento humano, além de integração futura com acesso remoto.

## Notificações

Eventos previstos: certificado próximo do vencimento, boleto vencido, estoque baixo, produto sem giro, cliente inativo, conta vencendo, rejeição fiscal e sincronização de PDV parada.

## Dados e banco

Banco principal PostgreSQL e SQLite local por terminal PDV. Grupos lógicos de dados:

| Domínio | Principais entidades |
|---|---|
| Empresa | empresas, filiais, usuários, perfis, permissões |
| Clientes | clientes, endereços, contatos, histórico, CRM |
| Produtos | produtos, grades, preços, estoques, movimentos, fiscal, combos |
| Vendas | vendas, itens, pagamentos, vendedores, caixas, cancelamentos |
| Financeiro | pagar, receber, lançamentos, bancos, cartões, conciliação |
| Fiscal | documentos, itens fiscais, XML, eventos, certificados, rejeições |

## Multiempresa, segurança e LGPD

O TRAXUP é multi-tenant: os dados de um cliente não podem ser expostos a outro. Dentro de cada empresa, permissões definem o que caixa, gerente, financeiro, fiscal e administrador podem consultar ou executar. Auditoria, autenticação, proteção de dados e LGPD são requisitos transversais.

## Integração entre módulos

Uma venda deve alimentar, conforme a operação: estoque → financeiro → fiscal → comissão → CRM → BI → contabilidade. O objetivo é evitar ilhas de informação.

## Arquitetura em linguagem simples

ERP Web, PDV e apps conversam com os serviços TRAXUP na nuvem. Esses serviços aplicam regras, salvam informações e integram fiscal, financeiro, estoque, CRM/BI e parceiros externos. O frontend web usa Angular 19; o motor de serviços usa Java/Spring Boot; PostgreSQL guarda os dados centrais; SQLite garante operação local do PDV; Redis/Kafka apoiam comunicação e processamento; armazenamento compatível com S3 guarda XML/DANFE.

## Central TRAXUP

A Central é a fonte de acompanhamento do próprio produto. Deve mostrar Dashboard, Ecossistema, Módulos, Funcionalidades, Regras de Negócio, Dependências, Testes, Documentação, Roadmap, Histórico, GitHub, Referências Visuais e Configurações. Ela deve distinguir claramente o que está especificado, aprovado visualmente, em implementação, testado e entregue.

## Fluxo visual e implementação

Proposta visual → revisão visual/funcional → aprovação visual → organização da referência no GitHub. A implementação Angular, integração com backend e testes funcionais seguem no Projeto - TraxUP. Imagem aprovada é referência de interface, não prova de funcionalidade pronta.

## Roadmap funcional de alto nível

1. Fundação/infraestrutura e segurança.
2. Empresa, filiais, usuários e permissões.
3. Clientes e produtos.
4. Estoque e compras.
5. Fiscal.
6. Vendas e PDV.
7. Financeiro e conciliação.
8. CRM, relatórios e BI.
9. Apps e integrações.
10. Administração SaaS, automações e evolução de IA.

As frentes podem avançar em paralelo quando as dependências permitirem.

## Visão futura de IA

A IA poderá responder perguntas de negócio, detectar produtos com queda de venda, filiais abaixo da meta, contas a receber, clientes inativos e risco de falta de estoque, tornando o ERP mais proativo.

## Objetivo final

Compra → estoque → venda → recebimento → documento fiscal → financeiro → relacionamento → análise → contabilidade, dentro de um único ecossistema TRAXUP, com administração SaaS para planos, cobrança, suporte e crescimento da plataforma.
