# TRAXUP — Homologação contínua

## Situação inicial — 15/09/2026

Branch dedicada: [homologacao](https://github.com/DjonesErison/ERP_TRAXUP-/tree/homologacao).
Base integrada auditada: `develop`, commit `6d5b76e1c7f3ae1f374075fa2625b356310b0236`.
Esta base é candidata inicial: integração não comprova aceite funcional de todos os módulos. A primeira publicação fica bloqueada até revisão do escopo e aprovação explícita do SHA. Nenhuma feature aberta foi incorporada para montar esta branch.

A PR [#324](https://github.com/DjonesErison/ERP_TRAXUP-/pull/324) está excluída: aberta, head `1479282befbdf9d04fba74a6e274fdec218717bc`, CI com falha na consulta. Não foi alterada nem incorporada. Build bem-sucedido isolado não dispensa validação funcional.

## Auditoria e arquitetura real

- 343 branches inventariadas; nenhuma branch dedicada equivalente a homologação encontrada.
- `main` em `1311791cb822050af110af9ecf935d1c6e8296b4`: Central Angular na raiz, Documento Mestre, projeto visual e deploy da Central. Não é atualmente uma versão integrada do ERP.
- `develop`: ERP em `tplug-erp/backend` (Spring Boot/Java 21), `tplug-erp/frontend` (Angular 19), PostgreSQL 17 e 103 migrations existentes. Fiscal e PDV são módulos do backend.
- Os workflows existentes testam frontend/backend em `develop` com filtros por caminho; o head consultado tem build Angular aprovado, mas isso não comprova execução de todos os testes nesse SHA.
- O Dockerfile raiz publica a Central; seu deploy usa o environment `production` e o domínio `central.traxup.com.br`. Nenhum desses contratos foi modificado.
- O Compose anterior sobe apenas PostgreSQL. O novo Compose em `tplug-erp/infra/homologacao/` sobe banco, API e frontend, no projeto Docker `traxup-homologacao`, porta local 8082 e volume próprio. Não publica a porta do banco ou da API.
- Consulta de rulesets respondeu 403, exigindo GitHub Pro ou repositório público. As branches consultadas estão sem proteção. Não foi possível ativar proteção obrigatória. O gate de publicação não substitui proteção de branch: quem tem escrita ainda pode modificar a branch/workflow. Restringir colaboradores com escrita e habilitar proteção quando disponível.

## Fluxo a cada entrega

1. `feature/*` ou `codex/*` → PR para `develop`: revisar escopo, testes, permissões, isolamento por tenant e evidências. Não integrar funcionalidade incompleta.
2. Criar `release/*` a partir de `homologacao`. Levar somente commits de funcionalidades finalizadas e suas dependências revisadas. Se `develop` contiver itens incompletos, selecionar commits; nunca sincronizá-la inteira por rotina automática.
3. Abrir PR de `release/*` para `homologacao`. Descrever funcionalidades entregues, PRs de origem, dependências, migrations, riscos, roteiro de validação e links para evidências. Alterações de aplicação exigem revisão funcional prévia; não basta marcar uma caixa.
4. Os checks `Homologacao - backend` e `Homologacao - frontend e smoke` devem passar para a versão final da PR. Um mantenedor confere as evidências e aplica o rótulo `homologacao-aprovada`. Fazer merge apenas depois dessa conferência. Se o head mudar, revisar novamente. Com proteção disponível, exigir ambos os checks, review e impedir force-push/exclusão.
5. O push em `homologacao` testa novamente a versão resultante, verifica PR mesclada com destino correto e rótulo, e só então publica as imagens já testadas no GHCR. Não há rebuild entre teste, publicação e deploy. O job de deploy depende dessas etapas e usa digests imutáveis.
6. Deploy no environment `homologacao`: servidor e segredos exclusivos. A validação automática confere `/build-info.json`, página inicial e resposta 400 da API de login para uma requisição vazia. Isso comprova disponibilidade básica, não substitui login real e testes de negócio.
7. O usuário acessa a URL registrada no deployment do GitHub. Na PR de promoção, registrar SHA, data, resultado por funcionalidade, evidências e aceite/reprovação. Falhas voltam como correção via PR; não promover a produção.
8. Produção: promover os mesmos digests aprovados. **Não fazer merge global `homologacao → main` hoje**: `main` publica a Central e contém documentos divergentes. A integração de fontes com `main` exige PR própria, conciliação da Etapa 0 e pipeline ERP de produção dedicado. Até lá, produção ERP permanece bloqueada; o deploy atual da Central continua independente.

## Primeira liberação

O workflow não considera a base inteira funcionalmente aprovada. Revisar os módulos presentes e as evidências antes de autorizar a primeira versão. Se houver escopo incompleto exposto, preparar uma PR que o retire da entrega ou selecione uma base menor, com testes. Não usar a variável de bootstrap para contornar essa análise.

Depois da validação da base e dos checks, o administrador pode definir a variável de repositório `HOMOLOGACAO_BASELINE_SHA` com o SHA completo e exato aprovado e reexecutar o workflow desse SHA. Essa exceção só autoriza a primeira publicação; remover a variável após o bootstrap. Entregas seguintes passam por PR rotulada. A PR #324 só pode entrar por nova promoção após CI e validação aprovados.

## Provisionamento do ambiente

Destino confirmado pelo usuário: **https://apphomologacao.traxup.com.br**, na **mesma VPS da Central**. O endereço está fixado no workflow e nos arquivos de proxy desta pasta. Acesso SSH e configuração efetiva do servidor ainda precisam ser disponibilizados ao environment `homologacao`.

A Central usa os segredos `TRAXUP_DEPLOY_HOST`, `TRAXUP_DEPLOY_USER`, `TRAXUP_DEPLOY_PORT` e `TRAXUP_DEPLOY_SSH_KEY` no environment `production`. Para a homologação, cadastrar o mesmo host/porta e um acesso autorizado à mesma VPS no environment `homologacao`, com os nomes abaixo. Se for reutilizada a credencial existente, cadastrá-la pelo gerenciador seguro original: segredos salvos no GitHub não podem ser recuperados em texto para cópia. Não alterar o environment `production` nem expor os segredos em logs.

Criar/configurar o environment GitHub `homologacao` (quando suportado pelo plano). Configurar:

| Local | Nome | Conteúdo |
|---|---|---|
| Variável do repositório | `HOMOLOGACAO_DEPLOY_ENABLED` | `true` somente após provisionamento |
| URL versionada no workflow | `https://apphomologacao.traxup.com.br` | Domínio confirmado para o ERP |
| Segredo do environment | `HOMOLOGACAO_SSH_HOST` | Host do servidor |
| Segredo do environment | `HOMOLOGACAO_SSH_USER` | Usuário de deploy |
| Segredo do environment | `HOMOLOGACAO_SSH_PORT` | Porta SSH, padrão 22 |
| Segredo do environment | `HOMOLOGACAO_SSH_KEY` | Chave SSH exclusiva |
| Segredo do environment | `HOMOLOGACAO_SSH_KNOWN_HOSTS` | Chave pública do host verificada fora do workflow |

No servidor Linux com Docker Compose v2, curl e flock:

1. Criar `/opt/traxup-homologacao` acessível ao usuário de deploy.
2. Copiar `.env.example` para `/opt/traxup-homologacao/.env`, restringir acesso (`chmod 600`) e preencher senha do banco e JWT próprio (mínimo 32 bytes). Nunca colocar segredos em PR/chat.
3. Autenticar o Docker do usuário de deploy no GHCR com credencial de leitura dos dois pacotes privados. O workflow usa seu `GITHUB_TOKEN` somente para publicar.
4. Apontar o DNS de `apphomologacao.traxup.com.br` para a mesma VPS da Central. Configurar o proxy que já administra HTTPS nesse servidor para `127.0.0.1:8082`: usar o trecho [Caddyfile](../../tplug-erp/infra/homologacao/Caddyfile) se a VPS usa Caddy, ou [nginx-site.conf](../../tplug-erp/infra/homologacao/nginx-site.conf) se usa Nginx. Não instalar um segundo proxy disputando as portas 80/443. Validar a configuração completa antes de recarregar o serviço e manter o host da Central intacto. Configurar backups do volume exclusivo antes de novas migrations.
5. Na base vazia, usar bootstrap administrativo documentado em [ambientes e deploy](ambientes-e-deploy.md). Desabilitar `BOOTSTRAP_ADMIN_ENABLED` depois de provisionar; remover a senha de bootstrap do arquivo e recriar o backend. Obter o tenant criado para login; não pressupor um tenant de produção.
6. Habilitar deploy e reexecutar o workflow da versão aprovada. Conferir o deployment no GitHub e executar login real, RBAC e o roteiro de cada entrega.

O frontend usa `/api/` na mesma origem e Nginx encaminha para o backend. Fiscal storage permanece desabilitado por padrão; integrações externas não são declaradas prontas por este deploy. Não usar dados ou credenciais de produção.

## Versão, falha e rollback

- `/build-info.json` expõe o commit; o resumo do deploy lista URL e digests. `release.env` no servidor registra a última versão com smoke local aprovado, e `previous-release.env` preserva a anterior.
- Se o smoke falhar, o workflow falha e não declara sucesso. Não há rollback automático de banco ou de aplicação após migrations: a versão nova pode ter sido iniciada e exige avaliação. Um erro na validação pública também exige verificar proxy/domínio.
- Para rollback, revisar compatibilidade do schema e usar os digests anteriores com o script `deploy.sh`. Se necessário, restaurar backup em procedimento operacional próprio. Nunca apagar volume ou editar migrations aplicadas como forma de rollback.

## Preservação da Etapa 0

Nenhum diretório foi reorganizado, migration alterada ou arquivo da Central substituído. O [Documento Mestre em main](https://github.com/DjonesErison/ERP_TRAXUP-/blob/main/docs/TRAXUP-DOCUMENTO-MESTRE.md), catálogo visual e evidências continuam sendo as fontes existentes. Esta documentação trata de entrega e ambiente, sem criar roadmap paralelo. A conciliação `main/develop` segue na Etapa 0.

### Configuração do proxy no servidor existente

Os arquivos de proxy são preparados para instalação, não comprovam alteração na VPS. Confirmar o proxy em uso antes de aplicar:

- **Caddy:** adicionar o bloco `Caddyfile` à configuração existente (ou a um diretório já importado), validar com `caddy validate --config /etc/caddy/Caddyfile` e recarregar o serviço. O domínio precisa resolver para a VPS e as portas 80/443 estar disponíveis para emissão do certificado.
- **Nginx:** instalar o virtual host HTTP preparado, executar `nginx -t` e recarregar o serviço. Emitir/configurar o certificado para `apphomologacao.traxup.com.br` pelo mecanismo ACME já usado na VPS (por exemplo, Certbot), habilitando HTTPS e redirecionamento. A configuração HTTP isolada não conclui a publicação HTTPS.
- Se o proxy roda em container com rede bridge, `127.0.0.1:8082` aponta para o próprio container: adaptar a ligação à rede/host efetivamente utilizada, após inspecionar o deploy da Central. Não aplicar esse upstream sem conferir essa condição.

Após configurar DNS/TLS, o smoke público do workflow precisa aprovar o SHA esperado em HTTPS; login real e validação funcional permanecem obrigatórios.
