# UI-015 — tela oficial e acesso direto

A referência é docs/design/13-identidade-visual-2.0/Ativação de Conta TRAXUP.png na main, sem modificar main. Cópia binária exata servida em assets/Ativacao-de-Conta-TRAXUP.png; logo atual assets/Logo.png preservada. Painel oficial com textos, ícones e notebook mantido.

/ativar mantém a UI-015 mesmo sem token, mostrando orientação e reenvio de ativação. Sem token, botão desabilitado e confirmação não chama a API. Links válidos mantêm validação das senhas, força, confirmação e remoção do fragmento da URL. Após sucesso, volta ao login. Login oferece acesso explícito à ativação.

Testes: acesso direto em 320, 390, 1280 e 1536 pixels, ausência de login indevido, bloqueio sem token e capturas; regressões de trial/ativação/recuperação preservadas. CI e deploy registrados no PR.
