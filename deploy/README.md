# Deploy automático — TRAXUP Central

O workflow `TRAXUP Central - Deploy` é disparado somente depois que `TRAXUP Central - Docker` termina com sucesso na branch `main`.

## Secrets necessários no environment `production`

- `TRAXUP_DEPLOY_HOST`: hostname ou IP da VPS.
- `TRAXUP_DEPLOY_USER`: usuário SSH dedicado ao deploy.
- `TRAXUP_DEPLOY_PORT`: porta SSH; opcional, usa 22 quando vazio.
- `TRAXUP_DEPLOY_SSH_KEY`: chave privada SSH exclusiva do deploy.

Não colocar esses valores no frontend, no repositório ou neste arquivo.

## Preparação recomendada da VPS

Use um usuário dedicado, por exemplo `traxup-deploy`, com acesso SSH por chave e permissão mínima necessária para executar Docker. Evite usar `root` no workflow.

A chave pública correspondente a `TRAXUP_DEPLOY_SSH_KEY` deve ser adicionada ao `~/.ssh/authorized_keys` desse usuário na VPS.

O usuário precisa conseguir executar `docker pull`, `docker run`, `docker rm`, `docker ps` e `docker container inspect` sem interação.

## Fluxo

1. Merge na `main`.
2. Workflow Docker constrói e publica `latest` e a tag imutável `${github.sha}`.
3. Somente após Docker verde, o workflow Deploy conecta à VPS.
4. O deploy usa a tag exata do commit, não `latest`.
5. O container é recriado em `127.0.0.1:8081:80`.
6. São executados health checks local e público.
7. Em caso de falha, o script tenta restaurar a imagem anterior.

## Produção

- Container: `traxup-central`
- Porta: `127.0.0.1:8081 -> 80`
- URL pública: `https://central.traxup.com.br`
- Imagem: `ghcr.io/djoneserison/traxup-central:<commit-sha>`

Antes de habilitar o primeiro deploy automático, configure o environment `production` e seus secrets no GitHub e prepare o usuário SSH da VPS.
