# Monitor VPS TRAXUP — homologacao

Executa no push dos arquivos do monitor em homologacao e por workflow_dispatch com ref homologacao. PRs executam somente testes isolados de sanitização, sem Secrets ou SSH. Não há agendamento automático: GitHub agenda workflows apenas a partir da branch padrão; main está fora do escopo.

Environment único: homologacao. Usa apenas os cinco Secrets HOMOLOGACAO_SSH_HOST, HOMOLOGACAO_SSH_KEY, HOMOLOGACAO_SSH_KNOWN_HOSTS, HOMOLOGACAO_SSH_PORT, HOMOLOGACAO_SSH_USER. Nenhum valor é consultado pela integração ou incluído em documentação. O runner usa arquivos temporários privados e valida estritamente a chave do host; não usa ssh-keyscan nem aceita host desconhecido.

Na VPS requer python3, docker e curl. Lista containers somente por labels exatas do projeto Compose traxup-homologacao e serviços frontend/backend/postgres. Mais de um container por serviço bloqueia a inspeção desse serviço. Lê somente State.Status; não lê ambiente, logs, volumes, .env ou dados de usuários. PostgreSQL é verificado com pg_isready, sem SQL. Não instala ferramentas nem copia arquivos para a VPS.

Faz GET local em / e /build-info.json na porta fixa 8082. GET /api/v1/auth/login espera 405 (rota conhecida somente POST): comprova resposta da rota, não autenticação ou saúde completa do backend. GET HTTPS na origem fixa apphomologacao.traxup.com.br não segue redirecionamentos. Apenas o SHA válido de 40 caracteres é retido do build-info. Nenhuma resposta HTTP bruta ou saída SSH é publicada.

Artefato traxup-homologacao-monitor contém report.json/report.md, inclusive em falhas tratadas. Relata SSH, containers, prontidão PostgreSQL, códigos HTTP, commit observado e limites. Falha de qualquer verificação básica resulta em run vermelho; não equivale a deploy realizado ou aceite funcional. O SHA do monitor pode diferir do SHA da aplicação pois esta PR não publica aplicação. Testes de negócio permanecem no CI da PR e não são executados na VPS.

O workflow existente homologacao.yml permanece intacto. Seu gate de publicação requer PR com homologacao-aprovada. Esta alteração operacional não recebe esse rótulo automaticamente nem autoriza deploy. Conferir checks backend/frontend do head final e regras de revisão antes do merge; nenhuma alteração em main/production.
