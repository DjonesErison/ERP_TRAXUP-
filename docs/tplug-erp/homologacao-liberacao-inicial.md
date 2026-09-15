# Liberação inicial para homologação

Em 15/09/2026, o responsável autorizou prosseguir com a primeira publicação de homologação após receber o resultado dos testes.

Base testada: `2b46df75fb86bfb24c35a7f8fbb4c81e27274423`.
Evidência: https://github.com/DjonesErison/ERP_TRAXUP-/actions/runs/35014778857

- Backend, migrations e imagem Docker: sucesso.
- Frontend, imagens e smoke da aplicação completa: sucesso.
- PR #324: não incorporada.
- Destino: https://apphomologacao.traxup.com.br, mesma VPS da Central, projeto Docker e banco próprios.

A autorização permite publicar a candidata para validação pelo usuário; não constitui aceite de produção de todos os módulos. O deploy depende dos checks da versão resultante, das credenciais do environment homologacao e do provisionamento documentado. A publicação agora é tentada automaticamente após os gates de testes e aprovação; a variável HOMOLOGACAO_DEPLOY_ENABLED deixa de ser necessária.

A Central, main, develop e o roadmap permanecem preservados. A validação de negócio e o aceite de produção serão registrados após acesso ao ambiente.
