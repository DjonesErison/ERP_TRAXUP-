# PR #401 — Logomarca oficial TRAXUP 2.0

O login desktop e mobile utiliza `docs/design/13-identidade-visual-2.0/Logo.png`, sem recriar ou modificar a imagem aprovada.

## Correção do smoke visual

O teste ainda esperava largura natural de 1200 pixels da imagem anterior. O cabeçalho PNG (IHDR) do arquivo oficial foi conferido: **1536 × 1024 pixels**, blob `5f57aa4c49f0f42d5e5b922851d4d390f6594bbe` em `main` no momento da verificação. A consulta foi somente leitura.

O teste agora seleciona a logo visível dentro do login, confirma o caminho oficial e valida largura e altura naturais em ambos os viewports existentes: 1536 × 1024 e 390 × 844. As verificações de autenticação, trial, navegação e ausência de overflow foram preservadas.

## Entrega

Integração exclusivamente em `homologacao`, após CI verde. A execução de homologação valida backend, migrations, imagens Docker, login, smoke visual e trial. O deploy permanece no workflow existente, restrito aos serviços de homologação. Resultados de CI e deploy ficam registrados no PR e no GitHub Actions.

O redesenho completo do login e a validação da UI-015 serão tratados em correções separadas, usando somente referências aprovadas.
