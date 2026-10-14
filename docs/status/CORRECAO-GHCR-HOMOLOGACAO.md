# Autenticação GHCR da homologação

Falha confirmada na execução 38496523059: pull remoto do backend retornou registry denied. Workflow não autenticava o Docker na sessão SSH. A etiqueta homologacao-aprovada ausente foi corrigida no PR 414; backend/frontend e testes de layout passaram.

Correção: GHCR_USERNAME/GHCR_TOKEN pelo stdin criptografado do SSH, login --password-stdin antes de deploy.sh e DOCKER_CONFIG temporário com limpeza ao encerrar. Não altera credenciais Docker existentes nem outros serviços. Secrets já cadastrados no repositório devem ter acesso aos pacotes traxup-erp-backend e traxup-erp-frontend; valores não foram lidos.

Validação: YAML, bash -n e quatro cenários simulados (sucesso, falha de login, falha de deploy e SHA inválido), verificando ordem, token ausente dos argumentos/saída, permissões 0700 e limpeza do diretório. Teste real remoto pendente após merge autorizado.
