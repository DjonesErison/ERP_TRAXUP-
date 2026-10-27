# Fluxo oficial de homologação — Trial e ERP

1. Cadastro de trial: https://apphomologacao.traxup.com.br/trial
2. Ativação: https://apphomologacao.traxup.com.br/ativar — usar o link com token enviado por e-mail.
3. Login: https://apphomologacao.traxup.com.br/entrar
4. Configuração inicial: https://apphomologacao.traxup.com.br/onboarding
5. Dashboard: https://apphomologacao.traxup.com.br/dashboard

Os e-mails de ativação e recuperação enviados pelo ambiente de homologação utilizam a origem `https://apphomologacao.traxup.com.br`, mantendo o token no fragmento do link. O endereço de configuração anterior `/configuracao` permanece aceito. O subdomínio `captacaoclientes.traxup.com.br` continua disponível por compatibilidade; não representa um ambiente de produção separado.

Deploys de homologação não executam a antiga limpeza de simulações automaticamente: os cadastros atuais devem ser preservados. Limpezas exigem procedimento separado e backup verificado.

O e-mail de cadastro de trial contém somente o link de ativação; o código da empresa e o e-mail cadastrado continuam no texto. Após criar a senha, o sistema direciona o usuário para a tela de login.
