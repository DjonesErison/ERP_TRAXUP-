# Fase 7 — PDV operacional e integração fiscal

A Fase 7 formaliza a próxima frente do TPlug ERP após a conclusão do inventário/mobile. O objetivo é construir a base do PDV operacional sem misturar responsabilidades fiscais, preservando operação offline, isolamento multi-tenant e rastreabilidade.

Esta fase consolida requisitos já definidos para o produto. A TRAXUP Central permanece separada e sem alteração de runtime.

## Bloco 1 — Contrato de terminal e série do PDV

- cadastro lógico de terminal vinculado a tenant e filial;
- identificação estável do terminal para sincronização;
- série própria por terminal para evitar colisão entre PDVs da mesma loja;
- estado ativo/inativo e auditoria de alterações críticas;
- nenhuma confiança em tenant informado manualmente pelo cliente: o escopo continua derivado da autenticação.

## Bloco 2 — Venda local e fila de sincronização

- banco SQLite local por terminal;
- venda deve continuar operacional durante perda temporária de comunicação com a nuvem;
- operações pendentes permanecem em fila local até confirmação do servidor;
- sincronização idempotente para impedir duplicidade de vendas em reenvios;
- cada venda carrega identificadores estáveis de terminal, série e operação local;
- conflitos devem ser detectáveis e auditáveis, nunca resolvidos silenciosamente com perda de dados.

## Bloco 3 — Consulta e últimas vendas

- consulta das vendas recentes conforme permissões do usuário;
- detalhamento de itens, totais e formas de pagamento;
- ações posteriores, como reimpressão, cancelamento e alteração de pagamento, devem possuir permissões próprias e regras explícitas;
- segunda via de comprovante deve reutilizar a venda persistida, sem criar nova operação comercial.

## Bloco 4 — Configuração operacional do PDV

Parâmetros por tenant/filial/terminal, conforme aplicável:

- exigência de justificativa e/ou autorização para cancelamento;
- fechamento de caixa;
- tamanho e destino de impressão;
- impressão para caixa/cozinha quando o segmento utilizar esse fluxo;
- preparação para autodetecção de impressoras no aplicativo Windows, sem acoplar o backend a drivers específicos.

## Bloco 5 — Módulo fiscal isolado

A emissão de NFC-e/NF-e deve permanecer isolada do núcleo comercial do PDV.

Responsabilidades previstas:

- contrato interno entre venda e módulo fiscal;
- emissão NFC-e e NF-e;
- ambientes de homologação e produção separados;
- contingência quando internet ou SEFAZ estiver indisponível;
- retomada/transmissão posterior sem duplicidade;
- armazenamento de XML e DANFE em repositório compatível com S3;
- correção assistida de rejeições sem alterar silenciosamente dados fiscais já autorizados.

Nenhuma regra tributária específica deve ser inventada no frontend ou no PDV. Regras fiscais pertencem ao módulo fiscal e devem ser versionadas/testadas.

## Bloco 6 — Assistente de homologação fiscal

- produto fiscal padrão de homologação (Produto Teste/Bola) para validar NF-e/NFC-e;
- onboarding identifica quando a empresa ainda precisa homologar emissão;
- fluxo orienta testes sem contaminar dados de produção;
- credenciais, certificados e segredos fiscais nunca são gravados em logs ou auditoria em texto aberto.

## Bloco 7 — Evoluções de atendimento

Após a base transacional e fiscal estar estável:

- mesas/comandas;
- delivery manual e integrações externas;
- segunda tela interativa do cliente (PDV-SEG-001);
- autoatendimento/self-checkout;
- POS integrado e Tap to Pay/NFC quando suportado pelo provedor/dispositivo.

Essas evoluções reutilizam o mesmo núcleo de venda e não devem criar modelos paralelos de estoque, pagamento ou documento fiscal.

## Segurança e auditoria

- tenant sempre derivado do contexto autenticado;
- filial e terminal validados dentro do tenant;
- RBAC obrigatório para operações sensíveis;
- cancelamentos, reprocessamentos, ajustes de pagamento e ações fiscais críticas auditados;
- dados sensíveis de pagamento, certificados, tokens e segredos não entram em auditoria;
- sincronização deve ser idempotente e rastreável.

## Ordem de implementação

1. terminal/série e contrato de sincronização;
2. venda offline + sincronização idempotente;
3. últimas vendas e operações pós-venda;
4. configurações operacionais do PDV;
5. contrato e infraestrutura do módulo fiscal isolado;
6. homologação, contingência e tratamento de rejeições;
7. mesas, delivery, segunda tela e autoatendimento.

## Critério de conclusão

A Fase 7 será considerada concluída quando o PDV puder registrar venda com identidade própria de terminal, operar temporariamente offline, sincronizar sem duplicidade, consultar operações recentes e encaminhar documentos ao módulo fiscal isolado com fluxo seguro de homologação/contingência. As evoluções de mesas, delivery e autoatendimento podem ser entregues incrementalmente sobre essa base, conforme os blocos definidos acima.
