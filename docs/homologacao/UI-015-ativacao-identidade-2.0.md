# UI-015 — Ativação de Conta TRAXUP 2.0

## Referência exclusiva

`docs/design/13-identidade-visual-2.0/Ativação de Conta TRAXUP.png` é a arte aprovada usada para o painel institucional. O frontend exibe somente a área esquerda da própria imagem por um contêiner com overflow oculto, sem renderizar o formulário estático da direita. Texto, ícones e notebook desse painel pertencem à imagem oficial; não são substitutos antigos ou recriados.

A região superior com a marca embutida na composição é ocultada por CSS. A logomarca exibida é exclusivamente `docs/design/13-identidade-visual-2.0/Logo.png`, inteira e proporcional. Os PNGs aprovados não foram editados. A arte está em main; seu uso é somente leitura, sem alteração dessa branch.

## Formulário interativo

- Composição desktop de 800/1536 e 736/1536 conforme a referência.
- Indicador conectado com três círculos: Cadastro, Ativação e Acesso.
- Ícones vetoriais do componente existente nos campos, segurança e ações.
- Senha e confirmação têm botões acessíveis de mostrar/ocultar.
- Requisitos com círculos e confirmação visual; símbolos continuam opcionais.
- Força da senha, erros, bloqueio durante envio, API, limpeza de sessão/token, recuperação e retorno ao login preservados.
- Reenvio permanece disponível após erro e pela ação de ajuda.

A arte apresenta “Falar com o suporte”, mas nenhum canal de suporte foi identificado/configurado no repositório. A ação funcional usa “Ajuda com a ativação” e abre o reenvio existente. Não foi inventado e-mail, telefone ou link externo.

## Responsividade e validação

No celular, o painel institucional aprovado é exibido integralmente e proporcional antes do formulário. O teste gera capturas da tela inicial e do erro em 320, 390, 1280 e 1536 pixels. Verifica o caminho de ambas as imagens oficiais, dimensão natural da logo, indicador de etapa, ausência de overflow, senhas vazias/curtas/diferentes sem chamadas de API, símbolos opcionais, alternância de visibilidade e fluxo de ativação/retry.

Entrega via PR para `homologacao`, somente após CI verde e inspeção das capturas reais. Deploy pelo workflow existente, restrito à homologação. Nenhum serviço de outro projeto é alterado.
