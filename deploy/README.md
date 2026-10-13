# Deploy automático — TRAXUP Central

O workflow `TRAXUP Central - Deploy` é disparado somente depois que `TRAXUP Central - Docker` termina com sucesso na branch `main`.

## Secrets necessários no environment `production`

- `TRAXUP_DEPLOY_HOST`: hostname ou IP da VPS.
- `TRAXUP_DEPLOY_USER`: usuário SSH dedicado ao deploy.
- `TRAXUP_DEPLOY_PORT`: porta SSH; opcional, usa 22 quando vazio.
- `TRAXUP_DEPLOY_SSH_KEY`: chave privada SSH exclusiva do deploy.

## Secrets GHCR no repositório

Configure em Settings → Secrets and variables → Actions → Repository secrets:

- `GHCR_USERNAME`: `DjonesErison`.
- `GHCR_TOKEN`: token com acesso ao pacote `traxup-central`, `read:packages` e `repo`, conforme o acesso solicitado para o repositório privado.

Se existirem secrets com esses nomes no environment `production`, eles têm precedência; mantenha os valores consistentes. Não envie o token em mensagens ou comentários. O plugin GitHub usado nesta correção não oferece acesso à API de secrets; a configuração precisa ser feita na interface do GitHub.

Não colocar esses valores no frontend, no repositório ou neste arquivo.

## Preparação recomendada da VPS

Use um usuário dedicado, por exemplo `traxup-deploy`, com acesso SSH por chave e permissão mínima necessária para executar Docker. Evite usar `root` no workflow.

A chave pública correspondente a `TRAXUP_DEPLOY_SSH_KEY` deve ser adicionada ao `~/.ssh/authorized_keys` desse usuário na VPS.

O usuário precisa conseguir executar `docker pull`, `docker run`, `docker rm`, `docker ps` e `docker container inspect` sem interação.

## Fluxo

1. Merge na `main`.
2. Workflow Docker constrói e publica `latest` e a tag imutável `${github.sha}`.
3. Somente após Docker verde, o workflow Deploy conecta à VPS.
4. Na mesma sessão SSH, o workflow informa o usuário efetivo (`id -un`) e cria um `DOCKER_CONFIG` temporário exclusivo da Central, com permissões restritas. O usuário, SHA e token seguem pelo stdin criptografado do SSH; o token é consumido por `docker login --password-stdin`, sem entrar no comando remoto. Esse contexto é herdado pelo script de deploy e removido ao encerrar a sessão, inclusive em falhas. Não altera o login Docker existente de root ou de outros serviços.
5. O deploy usa a tag exata do commit, não `latest`.
6. O container é recriado em `127.0.0.1:8081:80`.
7. São executados health checks local e público.
8. Em caso de falha, o script tenta restaurar a imagem anterior.

## Produção

- Container: `traxup-central`
- Porta: `127.0.0.1:8081 -> 80`
- URL pública: `https://central.traxup.com.br`
- Imagem: `ghcr.io/djoneserison/traxup-central:<commit-sha>`

Antes de habilitar o primeiro deploy automático, configure o environment `production` e seus secrets no GitHub e prepare o usuário SSH da VPS.

## Correção da autenticação — 2026-10-13

A execução 38470261631 falhou no pull da imagem do commit `cbfd241f00e94b6596e15412d5a4d2e8ce236015`. O workflow não fazia login no GHCR antes do pull. O usuário SSH é determinado por `TRAXUP_DEPLOY_USER`; os logs mascaram seu valor e não exibem o Docker config efetivo. Portanto, a identidade exata e eventual divergência em relação ao login manual de root ainda dependem de validação na VPS. O sucesso manual informado confirma acesso para aquela credencial, mas não autenticação para a sessão do workflow.

Validação local: leitura YAML, sintaxe Bash e cinco cenários com SSH/Docker simulados: sucesso, falha de login (deploy bloqueado), falha de deploy, token ausente e SHA inválido. Verificados ordem login→deploy, token somente pelo stdin do Docker e ausente de seus argumentos/saída, permissões 0700 do diretório temporário e limpeza após sucesso/falhas.

A validação real do GHCR, dos secrets e dos health checks permanece pendente. Após configurar os secrets e autorizar o merge, execute novamente o fluxo Docker→Deploy na main e valide `https://central.traxup.com.br/`. Não executar deploy desta branch antes da aprovação.
