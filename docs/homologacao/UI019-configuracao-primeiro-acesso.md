# UI-019 — Configuração após o login ERP

URL de homologação: https://apphomologacao.traxup.com.br/configuracao

Fluxo: trial → e-mail → ativar senha → entrar no ERP → configuração inicial.
A rota exige sessão autenticada. O usuário sem sessão vê o login. Um trial pendente
abre automaticamente a configuração após entrar. O menu Configurações permite voltar.

## Tela e sequência

Painel responsivo com identidade aprovada, logo oficial, nome real da sessão,
progresso e quatro cartões, nesta ordem:

1. Dados da empresa: responsável, documento, razão social, nome fantasia, e-mail,
   telefone, segmento e lojas recuperados do trial. Complementos de endereço e filial
   são validados e persistidos por tenant na migration V111.
2. Configuração fiscal: grava regime/CRT, ambiente e séries nas APIs existentes de
   perfil fiscal da filial. O texto informa que certificado digital e CSC ainda são
   necessários antes da emissão. Este incremento não cria um upload de certificado,
   configuração CSC ou habilitação de emissão fiscal.
3. Usuários e permissões: consulta usuários e perfis reais, permite atribuir perfil
   explicitamente e registrar a revisão da equipe. Não cria usuários automaticamente.
4. Vendas e PDV: consulta/cria terminal e salva justificativa/autorização de cancelamento,
   impressão de caixa/cozinha e tamanho nas APIs operacionais existentes.

O progresso reflete empresa revisada, perfil fiscal salvo, equipe revisada e PDV salvo.
Concluir fecha o primeiro acesso; Continuar depois prepara a filial para entrada sem
marcar as etapas como concluídas. O próximo login retoma a configuração pendente.
Não há nome de cliente fixo nem etapa marcada como concluída sem dados persistidos.
O atendimento usa uma orientação de contato até existir um canal de suporte configurado.

## Validação

OnboardingIntegrationTest verifica dados do trial, isolamento entre duas empresas,
endereço persistido, adiamento sem conclusão e criação idempotente da filial.
configuracao-smoke.mjs verifica login protegido, ordem, preenchimento, validação,
progresso, adiamento e ausência de overflow em 1536 e 390 pixels; gera screenshots.
O workflow de homologação executa o novo teste junto aos testes existentes.

Migration V112 atribui ADMIN ao administrador registrado do trial quando ele ainda
não possui nenhum perfil, dentro do tenant correspondente. Novos trials usam o
mesmo bootstrap. Vínculos de perfil existentes são preservados. O login precisa
ser renovado para carregar permissões adicionadas a trials anteriores.
