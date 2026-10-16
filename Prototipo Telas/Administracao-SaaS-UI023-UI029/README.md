# Administração SaaS TRAXUP — UI-023 a UI-029

Substitui o protótipo administrativo único anterior pelas sete referências aprovadas no PR #424, commit `673ea324ac0cdbd4d6c590acd2f7c3ee06eda8d7`. Imagens e especificações permanecem intactas em `Prototipo Telas`.

## Abrir e testar

Baixe `TRAXUP-Administracao-UI023-UI029.html` e abra no navegador. Angular, Tailwind, exportadores e logo estão incorporados. Nenhuma CDN, API ou banco é necessário. A URL usa fragmentos para navegação e recarga por tela:

| Tela | Endereço no HTML | Implementação |
| --- | --- | --- |
| UI-023 Visão Geral | `#visao-geral` | Indicadores, receita ilustrativa, distribuição, funil e atalhos |
| UI-024 Clientes | `#clientes` | Somente assinaturas contratadas, filtros, cadastro, detalhes, plano/módulos, bloqueio e suporte |
| UI-025 Trials | `#trials` | Plano Completo por sete dias, etapas, vencimento, prorrogação, conversão, notas, ativação/contato/encerramento simulados |
| UI-026 Financeiro | `#financeiro` | Uma linha por cliente com sua fatura mais recente, recebimentos, filtros e ações financeiras |
| UI-027 Planos e Assinaturas | `#planos-assinaturas` | Cinco planos, edição de preço/limites, ciclo anual de referência, matriz e simulador |
| UI-028 Faturas | `#faturas` | Cobranças individuais, emissão, pagamento parcial/integral, cancelamento, vencimento e documentos demonstrativos |
| UI-029 Relatórios | `#relatorios` | Indicadores, filtros, análises, nomes de relatórios salvos e downloads reais `.pdf`/`.xlsx` |

Conversão de trial atualiza Clientes e cria uma fatura. Pagamento atualiza Financeiro e Faturas. Alterar plano atualiza preço mensal e registra upgrade/downgrade; não reajusta faturas já emitidas. Editar catálogo não reajusta contratos existentes. Corporativo requer negociação, sem preço automático.

## Desenvolvimento

Node.js 20.11+ compatível com Angular 19.

```bash
npm install
npm start
```

Abra `http://localhost:4200`. Para regenerar o HTML:

```bash
npm run build
```

Tailwind é compilado antes do Angular. O gerador reúne também os módulos de exportação em um único HTML. Logo oficial: `docs/design/13-identidade-visual-2.0/Logo.png`. Referência de Grid: https://tailwindcss.com/docs/grid-column.

## Verificação

```bash
npm run build
npx playwright install chromium
npm test
```

Opcional: `PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH` aponta para um Chromium existente. Teste abre o HTML por `file://`, verifica as sete rotas em 320, 390, 768 e 1536 px, ausência de erros JS e arquivos externos, separação Clientes/Trials, paginação, busca vazia, prorrogação inválida/válida, conversão integrada, mudança de plano/preço, pagamento inválido/parcial/integral, edição do catálogo, ciclo anual, simulador, downloads PDF/XLSX, relatório salvo e recarga de rota. Capturas em `test-results/`.

## Limites para validação

- Protótipo funcional com empresas fictícias e estado somente em memória. Recarregar restaura os dados iniciais; nomes de relatórios não persistem.
- Não há backend, autenticação, cobrança, e-mail, certificado ou acesso de suporte real. Pix copia apenas identificador marcado `DEMO-SEM-VALIDADE`. Segunda via e comprovante são TXT sem validade financeira.
- PDF e Excel exportam dados tabulares da análise, não uma reprodução gráfica dos painéis.
- Série histórica da receita é ilustrativa; distribuição do anel, contagens, valores e listas vêm da base local. Período seleciona a série ilustrativa, não um histórico contábil real. Filtro de plano dos relatórios afeta MRR, ARR, churn e a exportação tabular; painéis de contexto da base permanecem globais.
- CNPJ valida 14 dígitos e duplicidade, sem cálculo dos verificadores. Usuários e onboarding são resumos simulados. Matriz e adicionais comerciais são referências, sem contratação real.
- Nenhuma rota da aplicação publicada ou serviço da VPS foi alterado. Integração de produção e autenticação são uma etapa posterior.

## Revisão visual

Referências exclusivas: os sete arquivos `.webp` UI-023 a UI-029 e seus `.md` na raiz de `Prototipo Telas` em `main`. Revisão ajusta densidade, tipografia condensada, ícones SVG, cartões, menu e gráficos; não pressupõe aprovação visual. Fonte Roboto Condensed incorporada, licença SIL OFL em `src/assets/RobotoCondensed-OFL.txt`. A imagem oficial `Logo.png` é preservada sem filtros de cor ou recriação.
