# Trial e telas públicas em celular — 2026-10-13

Escopo: frontend ERP da branch homologacao. Central e serviços da VPS não alterados.

- Removidos links da Locaweb e ConnectPlug. Documentos oficiais TRAXUP precisam ser fornecidos/publicados; o aviso de homologação não constitui política ou contrato.
- Trial em até 900px usa logo e texto HTML compacto, preservando a arte no desktop. Formulário e estados de erro/sucesso mantidos.
- Benefícios abaixo do formulário em coluna em até 480px.
- Ativação em celular prioriza formulário, mantendo logo e etapas.
- Login protege largura do formulário e usa inputs de 16px para evitar zoom automático no iOS.

Merge/publicação pendentes de autorização. Testes funcionais de cadastro e ativação não incluídos na revisão visual.

## Validação

- Build Angular de produção passou (576,88 kB, abaixo do limite de 1 MB).
- git diff --check passou.
- Links de terceiros removidos do template ativo; logo local existente.
- Revisão de CSS: breakpoints 900px/820px/480px, inputs 16px e min-width:0.
- Renderização visual em viewport de celular ainda pendente; a ferramenta de navegador desta sessão não oferece redimensionamento. Deve ser conferida antes de considerar a versão mobile visualmente validada.
