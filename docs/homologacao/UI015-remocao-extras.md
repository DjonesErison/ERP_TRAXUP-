# UI-015 — remoção dos extras indicados pelo usuário

Removidos o aviso vermelho ao abrir /ativar sem token, a abertura automática do formulário de reenvio e o link adicional de acesso ao ERP no rodapé. A tela mantém o painel oficial, logo atual, etapas e formulário de ativação.

Reenvio disponível por ação explícita em Ajuda com a ativação ou em erro de confirmação. Botão de ativação continua desabilitado sem token e a API não recebe confirmação sem token. Testes existentes verificam ausência dos extras e preservam regressões desktop/celular. Merge somente após CI verde; acompanhar deploy. main preservada.
