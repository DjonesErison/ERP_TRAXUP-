# Administração SaaS — UI-023 a UI-029

Implementação integrada ao ERP, em `tplug-erp/frontend/src/app/administracao`, baseada exclusivamente nas sete imagens e especificações do PR #424, `Prototipo Telas`. O protótipo do PR #425 é a referência de componentes; não é usado como banco nem como página embutida.

## Acesso

Rota do ERP: `/administracao#visao-geral`. As outras telas usam `#clientes`, `#trials`, `#financeiro`, `#planos-assinaturas`, `#faturas` e `#relatorios`. Após publicação em homologação, a entrada será `https://apphomologacao.traxup.com.br/administracao`.

Usa o login e o interceptor JWT existentes. Dispensa a seleção de filial e o onboarding da empresa operadora. O cabeçalho do ERP mostra o link somente para operadores autorizados. O backend revalida a autorização em toda requisição; o frontend não concede permissões.

O operador global é explicitamente vinculado em `saas_operadores`. Nenhum administrador de cliente, perfil RBAC, usuário de trial ou usuário de bootstrap recebe esse acesso automaticamente. A migration deixa a lista vazia. A autorização inicial precisa ser executada pelo responsável pelo banco **apenas para o UUID de um usuário interno TRAXUP confirmado**:

```sql
-- Substituir o parâmetro por um UUID de usuário interno já conferido.
INSERT INTO saas_operadores(usuario_id,ativo) VALUES (:usuario_interno_confirmado,TRUE)
ON CONFLICT(usuario_id) DO UPDATE SET ativo=TRUE;
-- Revogação imediata, inclusive com JWT já emitido:
UPDATE saas_operadores SET ativo=FALSE WHERE usuario_id=:usuario_interno_confirmado;
```

Não há endpoint para um administrador de cliente criar operadores globais. Não executar essa autorização em contas de clientes ou de demonstração.

## Persistência e contratos

Migration `V113__administracao_saas.sql`: operadores, catálogo, assinaturas, faturas, recebimentos, eventos de auditoria e relatórios salvos. Não apaga dados existentes e não cria empresas fictícias. O catálogo inicial contém os cinco preços de referência aprovados para estudo comercial; Corporativo exige valor mensal negociado informado no contrato.

`GET /api/v1/saas/acesso` retorna apenas a elegibilidade do usuário atual. As demais operações em `/api/v1/saas` exigem autorização global:

- `GET /painel`: trials reais, contratos, faturas, catálogo e relatórios do operador.
- `POST /trials/{id}/prorrogar`, `/converter`, `/encerrar`, `/notas`.
- `POST /clientes/{id}/plano`, `/bloqueio`.
- `POST /faturas`, `/faturas/{id}/pagamentos`, `/vencimento`, `/cancelar`.
- `PUT /planos/{nome}` e `POST /relatorios`.

Valores monetários usam `NUMERIC`/`BigDecimal`. A emissão e os pagamentos manuais usam UUID idempotente; os registros são bloqueados durante alterações concorrentes. Conversão é transacional e cria uma única assinatura com sua primeira fatura. Alterações do catálogo preservam contratos existentes; alterações de plano preservam faturas emitidas. Não permite pagamento superior ao saldo nem cancelamento de fatura com recebimentos.

Clientes são trials efetivamente convertidos em contrato. Empresas antigas sem origem no cadastro de trial não são convertidas automaticamente: exigem migração comercial explícita. O cadastro de novas empresas utiliza o formulário de trial existente, com ativação e posterior contratação. Todas as alterações são confirmadas pelo servidor antes de atualizar a interface.

Bloqueio suspende o tenant e é verificado também para JWTs anteriores à suspensão. Encerramento de trial também bloqueia as APIs autenticadas daquele tenant. O operador não pode bloquear a própria empresa. A liberação precisa ser realizada pela administração. Inadimplência deriva do saldo e vencimento das faturas; contratos ativos em atraso continuam compondo o MRR até suspensão. Limites e módulos do contrato são registros comerciais; não substituem o RBAC operacional nem implementam cobrança automática de excedentes.

## Dados e limitações explícitas

- As sete telas leem dados persistidos. Os gráficos usam faturas emitidas por competência, não séries inventadas; o título indica faturamento, que não equivale a MRR histórico. MRR/ARR corrente derivam de contratos ativos.
- Primeira utilização, terminais e validade de certificados não são inventados; aparecem sem dados quando não existe fonte. Onboarding usa o status já persistido.
- Recebimentos são lançamentos administrativos manuais, não confirmação bancária. Documentos TXT representam registros internos; não são boletos ou documentos fiscais.
- Pix, envio de cobrança e impersonação de suporte permanecem indisponíveis até integração específica. Nenhuma ação finge gerar código Pix, contatar clientes ou assumir identidade.
- Reenvio de ativação permanece na jornada pública existente; o atalho administrativo não está integrado nesta entrega.
- Exportação tabular CSV compatível com Excel, com proteção contra fórmulas. O botão Imprimir/PDF usa a impressão do navegador. Não há arquivo XLSX nativo nesta entrega.
- Relatórios salvos persistem nome, plano e período, separados por operador. Salvar o mesmo nome atualiza esses filtros.
- Não há renovação recorrente automática, cancelamento de contrato com cálculo de churn histórico, desconto anual aplicado a contrato nesta entrega. O simulador e a matriz comercial permanecem referenciais.

## Validação e publicação

Testes em `SaasIntegrationTest`: autorização, revogação, tenant incorreto, conversão única, idempotência, saldo, catálogo, bloqueio e relatórios privados. Validação local: build Angular e testes do navegador passaram; 43 testes de SaaS, autenticação, RBAC e trial passaram no PostgreSQL.

Smoke administrativo verifica as sete telas, cabeçalho autenticado da API, falha e repetição de pagamento com a mesma chave, acesso negado e larguras 320/390/768/1680 px. Build Angular separa a administração em chunk carregado sob demanda. A fonte condensada aprovada possui licença em `src/assets/administracao/RobotoCondensed-OFL.txt`. A logo oficial existente não foi alterada.

O PR de implementação tem base `homologacao`. Não modifica `main`, serviços da Central nem outros projetos. Publicação depende do CI e do fluxo de homologação; o link de prévia com dados demonstrativos permanece separado da aplicação real.

## Indicadores de alteração de plano

Upgrades e downgrades são carregados dos eventos persistidos do contrato; recarregar não zera os contadores. Trocas de módulos ou regravações com o mesmo preço ficam no histórico como alteração, sem contar como upgrade. A consulta de elegibilidade administrativa ocorre depois da limpeza de sessão dos links públicos de ativação/recuperação.

## Ajustes às referências UI-023 a UI-029 (17/10/2026)

Fonte visual: imagens e especificações aprovadas em `Prototipo Telas`, PR #424. As sete rotas integradas continuam usando as APIs reais. A visão geral agora mantém as cores dos planos coerentes entre legenda e gráfico, o funil em quatro níveis com rótulos laterais e as ações com contagens reais. O indicador de inadimplentes é uma contagem de clientes. Os vencimentos mostram somente faturas abertas futuras e trials ativos, ordenados por data; não incluem faturas pagas ou canceladas. Ações de faturas em atraso incluem pagamentos parciais vencidos.

Usuário sem autorização recebe uma página própria, sem menu administrativo nem dados. Ela permite voltar ao ERP, trocar usuário ou verificar a liberação. Sessão expirada solicita novo login. Nenhum operador é habilitado automaticamente. A liberação ainda exige identificar o usuário interno TRAXUP.

O histórico de primeiro acesso e a integração com certificados permanecem indisponíveis, identificados na tela. O gráfico de faturamento usa faturas por competência, sem apresentar esses valores como histórico de MRR ou percentuais de inadimplência. Não são fabricados valores para reproduzir os números ilustrativos das imagens.

Validação: build Angular e smoke das sete telas em 320, 390, 768 e 1680 px; estados de acesso negado, nova verificação de acesso e sessão expirada; cores da distribuição, quatro etapas e próximos vencimentos.
