# UI-023 — Administração de clientes

Protótipo funcional Angular 19 + Tailwind CSS 4, baseado na imagem e especificação UI-023 da pasta `Prototipo Telas`. Reutiliza o `Logo.png` oficial sem alterar a arte.

## Validar sem instalar

Baixe `TRAXUP-UI023-Administracao-Clientes.html` e abra no navegador. O arquivo incorpora Angular, CSS e logo; funciona sem CDN e sem API. O GitHub mostra o código: baixe o arquivo antes de abrir.

## Executar e editar o Angular

Requer Node.js 20.11+ compatível com Angular 19.

```bash
npm install
npm start
```

Abra `http://localhost:4200`. Para gerar o HTML novamente:

```bash
npm run build
```

O Tailwind é compilado antes do Angular. `scripts/make-single-html.mjs` incorpora o resultado em um único HTML. Layout usa Grid e variantes responsivas do Tailwind, com CSS próprio para reproduzir o visual aprovado. Referência: https://tailwindcss.com/docs/grid-column.

## Roteiro de validação

- Conferir indicadores, seleção de empresa, paginação e filtros combinados de situação, plano, segmento, vencimento e módulos.
- Buscar nome, CNPJ ou responsável; testar resultado vazio e limpar filtros.
- Cadastrar cliente: campos obrigatórios, e-mail, CNPJ com 14 dígitos e duplicidade. A validação do CNPJ é estrutural e não verifica dígitos verificadores.
- Alterar plano e módulos; conferir resumo e logs. Alterar plano não calcula preço automaticamente.
- Selecionar um Trial, prorrogar 1–30 dias e conferir nova data; outros estados desabilitam a ação.
- Confirmar/cancelar bloqueio, liberar acesso, registrar cobrança simulada e baixar segunda via TXT sem validade.
- Exportar CSV das empresas filtradas ou selecionadas; abrir prévia de suporte somente leitura.
- Conferir abas Resumo, Faturamento, Certificados e Logs, notificações e restauração dos dados.
- Redimensionar para celular; tabela possui rolagem horizontal interna e detalhes ficam abaixo da listagem.

## Limites do protótipo

16 empresas fictícias, dados em memória: recarregar o arquivo restaura o estado inicial. Gráfico é uma série ilustrativa. MRR soma mensalidades dos clientes ativos. Nenhuma ação envia e-mail, cobra valores, altera acesso real, faz upload de certificado ou acessa conta de cliente. Navegação lateral demonstra filtros e diálogos da tela UI-023; não são módulos administrativos completos. Sem autenticação, backend, banco, rotas ou alterações na aplicação publicada.

## Teste automatizado

Após `npm install` e `npm run build`, execute `npx playwright install chromium` e `npm test`. O teste abre o HTML por `file://`, exercita os fluxos principais e verifica ausência de erros JavaScript, requisições externas e transbordamento da página em 320, 390, 768 e 1536 px. Capturas ficam em `test-results/`. É possível informar um Chromium existente em `PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH`.

## Correção visual para a referência aprovada

A revisão usa exclusivamente `Prototipo Telas/UI-023-administracao-clientes-traxup.webp`
e sua especificação `.md`. Mantém sidebar de 192 px, cinco indicadores em linha,
filtros e busca no cabeçalho da tabela, painel lateral alinhado à borda direita e
blocos inferiores nas proporções da referência. Os símbolos de fonte foram
substituídos por ícones SVG locais; nenhuma tela foi rasterizada.

O arquivo `Logo.png` é byte a byte o ativo oficial da branch main. A imagem é exibida
com suas cores originais, sem filtros ou recriação da arte. O aviso de
dados demonstrativos fica no rodapé, sem faixa adicional acima da tela aprovada.

O workflow `TRAXUP UI-023 - Prototipo` compila este projeto e executa o smoke
específico, além do CI independente da Central. Capturas incluem 1672 px,
320, 390, 768 e 1536 px. O PR permanece aberto para aprovação visual, sem deploy.
