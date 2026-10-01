# ADR — Etapa 1: jornada SaaS, tenant, empresa e filial

Status: decisão aprovada para orientar a Etapa 1.
Data: 17/09/2026.

## 1. Porta de entrada do TRAXUP

A jornada de um cliente novo começa antes do ERP autenticado.

Fluxo oficial:

`Captação 7 dias → Conta SaaS/Tenant → Empresa/CNPJ inicial → Administrador → ativação/login → onboarding → filiais → usuários/RBAC → contexto operacional → Dashboard/ERP`

A página pública de captação é separada da experiência autenticada do ERP, mas usa o mesmo backend de provisionamento/identidade.

Sugestão de exposição:

- `traxup.com.br/teste`: captação/trial público;
- `app.traxup.com.br`: login e ERP autenticado;
- homologação mantém domínio próprio do ambiente.

## 2. Trial de 7 dias

Ao aceitar o trial, o sistema deve criar de forma transacional/idempotente:

1. conta SaaS/tenant;
2. período de trial com início, expiração e situação;
3. empresa/CNPJ inicial quando os dados mínimos estiverem disponíveis;
4. usuário administrador proprietário;
5. vínculo do administrador ao tenant;
6. estado inicial do onboarding.

O trial não deve depender de cadastro manual no banco.

## 3. PostgreSQL compartilhado

A arquitetura padrão do SaaS utiliza um cluster/banco PostgreSQL compartilhado entre os clientes, com isolamento lógico multi-tenant.

Não criar um banco PostgreSQL por cliente na arquitetura padrão inicial.

Uma futura modalidade Enterprise poderá usar banco/infra dedicada sem alterar o modelo lógico da aplicação.

## 4. Conceitos separados

### Tenant / Conta SaaS

Representa o assinante/contrato lógico dentro da plataforma. É a fronteira primária de isolamento dos dados.

### Empresa

Representa uma pessoa jurídica/CNPJ pertencente ao tenant. Um tenant pode possuir uma ou mais empresas.

### Filial / Estabelecimento

Representa o contexto operacional de um estabelecimento. Deve pertencer a uma empresa e ao mesmo tenant. A modelagem deve permitir matriz e múltiplos estabelecimentos sem criar outro tenant apenas por existir outro CNPJ/filial.

### Usuário

Identidade humana de acesso. Permissões e vínculos determinam quais tenants/empresas/filiais e operações podem ser acessados.

## 5. Regra de isolamento

Dados de negócio tenant-scoped devem carregar `tenant_id` ou possuir vínculo estrutural que garanta inequivocamente o tenant.

O backend deriva o tenant do contexto autenticado e não confia em `tenant_id` arbitrário enviado pelo frontend.

Operações de filial validam cumulativamente:

- tenant autenticado;
- existência da empresa/filial no mesmo tenant;
- vínculo do usuário com a filial/contexto;
- permissão RBAC necessária.

Nenhuma consulta, relacionamento ou mutação pode cruzar tenants.

## 6. Integridade de banco

Sempre que aplicável:

- chaves estrangeiras e constraints devem impedir referência cruzada entre tenants;
- índices devem iniciar ou incluir `tenant_id` nas consultas tenant-scoped relevantes;
- unicidades de negócio devem ser compostas por tenant quando o valor só precisa ser único dentro do cliente;
- migrations são únicas para a plataforma, não executadas separadamente por cliente;
- backup e restore devem considerar requisitos de recuperação por tenant, mesmo com banco compartilhado.

## 7. Onboarding

Primeiro acesso de uma conta ainda não configurada deve conduzir ao onboarding, não diretamente ao Dashboard.

O onboarding persiste progresso no backend e cobre pelo menos:

1. confirmação/complemento da empresa;
2. configuração da matriz/primeira filial;
3. dados fiscais e operacionais mínimos exigidos para os módulos habilitados;
4. criação/convite de usuários adicionais, quando desejado;
5. perfis e permissões;
6. conclusão explícita.

Após conclusão, acessos normais seguem Login → seleção de contexto quando necessária → Dashboard.

## 8. Etapa 1 reorganizada

Nome: **Aquisição, Provisionamento e Primeiro Acesso**.

Entregáveis do pacote:

- tela pública de captação/trial de 7 dias;
- API de provisionamento idempotente/transacional;
- conta SaaS/tenant e ciclo do trial;
- empresa/CNPJ inicial;
- administrador inicial e ativação/definição de senha;
- Login e recuperação de senha;
- onboarding persistido;
- empresas e filiais;
- usuários, vínculos e RBAC;
- seleção de empresa/filial/contexto;
- Dashboard após onboarding;
- testes de isolamento multi-tenant e jornada ponta a ponta;
- homologação para validação como cliente novo.

## 9. Critério de conclusão

A Etapa 1 somente é concluída quando um usuário sem cadastro prévio consegue iniciar o trial pela página pública e percorrer a jornada completa até o ERP, sem intervenção manual no banco, mantendo isolamento de outro tenant de teste.
