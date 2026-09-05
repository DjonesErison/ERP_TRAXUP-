# TPlug ERP — Ambientes e Deploy

Este documento define o contrato mínimo de configuração e promoção do backend entre desenvolvimento, homologação e produção. O código e a documentação técnica permanecem no GitHub; segredos nunca devem ser versionados.

## Ambientes

### Desenvolvimento

Objetivo: execução local e integração durante desenvolvimento.

- Banco PostgreSQL local ou containerizado.
- `OPENAPI_ENABLED=true` permitido para facilitar testes manuais.
- `BOOTSTRAP_ADMIN_ENABLED=true` somente para criação inicial de uma base vazia; desabilitar após o primeiro provisionamento.
- Segredos locais ficam fora do Git e podem ser derivados de `tplug-erp/backend/.env.example`.

### Homologação

Objetivo: validar migrations, autenticação, RBAC, auditoria, integrações e a imagem que será promovida.

- Usar banco PostgreSQL dedicado ao ambiente.
- `JWT_SECRET` próprio e diferente de desenvolvimento/produção.
- `OPENAPI_ENABLED` deve ser decidido pelo responsável do ambiente; quando habilitado, a exposição deve ficar protegida pela infraestrutura apropriada.
- Bootstrap somente durante provisionamento inicial de uma instalação vazia.
- A imagem Docker deve ser construída a partir de commit aprovado e identificada por tag imutável, preferencialmente o SHA do commit.

### Produção

Objetivo: execução estável, auditável e reproduzível.

- Banco PostgreSQL dedicado, com backup e política de retenção definidos pela operação.
- `DB_PASSWORD` e `JWT_SECRET` fornecidos por secret manager ou mecanismo equivalente da infraestrutura.
- `OPENAPI_ENABLED=false` por padrão.
- `BOOTSTRAP_ADMIN_ENABLED=false` após o primeiro provisionamento.
- Nunca usar tag mutável como única referência de deploy; registrar o SHA/tag exata da imagem implantada.
- A aplicação deve executar com o usuário não-root definido no Dockerfile.

## Variáveis obrigatórias e recomendadas

Obrigatórias para execução normal:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`

Configurações com valores padrão seguros no backend:

- `JWT_ISSUER=traxup-tplug-erp`
- `JWT_ACCESS_TOKEN_MINUTES=15`
- `JWT_REFRESH_TOKEN_DAYS=30`
- `SERVER_PORT=8080`
- `OPENAPI_ENABLED=false`
- `BOOTSTRAP_ADMIN_ENABLED=false`

Variáveis de bootstrap, utilizadas apenas no primeiro provisionamento quando `BOOTSTRAP_ADMIN_ENABLED=true`:

- `BOOTSTRAP_ADMIN_TENANT_NAME`
- `BOOTSTRAP_ADMIN_NAME`
- `BOOTSTRAP_ADMIN_EMAIL`
- `BOOTSTRAP_ADMIN_PASSWORD`

## Fluxo de promoção

1. Toda alteração entra por branch e Pull Request para `develop`.
2. O CI executa testes Java, valida Flyway contra PostgreSQL 17 e valida o `docker build`.
3. Somente commit com CI verde pode ser considerado candidato a deploy.
4. A imagem deve ser identificada por tag imutável vinculada ao commit, por exemplo `tplug-erp-backend:<git-sha>`.
5. Homologação recebe primeiro a mesma imagem candidata.
6. Produção recebe exatamente a imagem aprovada em homologação, sem rebuild entre ambientes.
7. Antes do deploy, confirmar backup/recuperação do banco e revisar migrations pendentes.
8. Após o deploy, validar inicialização da aplicação, execução das migrations, autenticação e endpoints críticos.

## Exemplo de build e execução

A partir de `tplug-erp/backend`:

```bash
docker build -t tplug-erp-backend:<git-sha> .

docker run --rm \
  --env-file .env \
  -p 8080:8080 \
  tplug-erp-backend:<git-sha>
```

O arquivo `.env` real não deve ser commitado. Use `.env.example` apenas como referência de contrato.

## Checklist de deploy

- CI do commit está verde.
- Imagem corresponde exatamente ao commit aprovado.
- Segredos do ambiente estão configurados fora do repositório.
- `OPENAPI_ENABLED` está conforme a política do ambiente.
- `BOOTSTRAP_ADMIN_ENABLED=false`, exceto no primeiro provisionamento de base vazia.
- Backup/restore do banco está operacional antes de migrations de produção.
- Tag/SHA implantada foi registrada.
- Pós-deploy validou aplicação, migrations, login, autorização e auditoria.

## Rollback

Rollback de aplicação deve reutilizar uma imagem anterior conhecida e imutável. Rollback de banco não deve ser feito apagando ou alterando migrations Flyway já aplicadas. Quando uma migration exigir correção, a estratégia deve considerar uma nova migration compatível e, quando necessário, recuperação por backup conforme o impacto da mudança de dados/schema.
