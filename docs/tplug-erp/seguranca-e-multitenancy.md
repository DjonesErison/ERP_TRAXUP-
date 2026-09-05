# Segurança e Multi-tenancy

## Identidade e autenticação

O login atual usa `tenantId + email + senha`. E-mail é normalizado e sua unicidade é garantida dentro do tenant, não globalmente.

Senhas usam BCrypt. Em falha de autenticação, o serviço responde de forma genérica para não revelar se o usuário existe.

O access token JWT contém, entre outros dados, `sub` com o UUID do usuário, `tenant_id`, `email` e `permissions`. O segredo do JWT vem de variável de ambiente e deve possuir tamanho adequado para HS256.

Refresh tokens:

- são retornados em formato bruto somente ao cliente;
- são armazenados no banco apenas como hash SHA-256;
- possuem expiração;
- são rotacionados no refresh;
- são revogados no logout.

## Contexto do tenant

O backend considera o claim `tenant_id` do JWT autenticado como fonte do tenant. O antigo cabeçalho `X-Tenant-Id` não participa da autorização e não pode substituir o tenant do token.

Consultas de Empresa, Filial e Usuário usam tenant-scoping. O banco possui constraints adicionais para evitar vínculos cross-tenant em pontos críticos.

## RBAC

O RBAC atual é composto por:

- `permissoes`: catálogo global de permissões;
- `perfis`: perfis pertencentes a um tenant;
- `usuario_perfis`: associação tenant-safe entre usuários e perfis;
- `perfil_permissoes`: associação entre perfil tenant-scoped e permissão global.

Permissões efetivas são carregadas durante a emissão do JWT e convertidas diretamente em authorities do Spring Security.

Permissões já cadastradas no catálogo inicial:

- `EMPRESA_LER`
- `EMPRESA_CRIAR`
- `EMPRESA_DESATIVAR`
- `FILIAL_LER`
- `FILIAL_CRIAR`
- `FILIAL_DESATIVAR`
- `RBAC_GERENCIAR`

Atualmente Empresa e Filial já possuem `@PreAuthorize` por operação. Usuário ainda precisa receber permissões específicas em etapa posterior.

## Janela de validade de permissões

Como as permissões são incorporadas ao access token, uma alteração de perfil/permissão pode continuar válida em um token já emitido até seu vencimento. O access token padrão dura 15 minutos; refresh gera novo token com o conjunto atual de permissões.

## Auditoria

A auditoria é append-only por desenho de aplicação. Não há API de alteração ou exclusão de eventos.

Eventos podem registrar:

- tenant;
- usuário autenticado;
- empresa;
- filial;
- operação;
- entidade;
- UUID da entidade afetada;
- detalhes relevantes;
- timestamp UTC.

Nunca registrar em auditoria:

- senha ou hash de senha;
- refresh token bruto;
- JWT bruto;
- segredo de assinatura;
- credenciais de banco ou integração.

## Pendência crítica: bootstrap do primeiro administrador

Ainda não existe fluxo público de bootstrap/administração. Antes de expor a API de gestão do RBAC, deve ser definido um mecanismo seguro para criar o primeiro administrador do tenant sem abrir endpoint administrativo inseguro.
