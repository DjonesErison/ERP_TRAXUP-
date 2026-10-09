# UI-019 — Botão Voltar nas configurações

Cada uma das quatro etapas possui um botão Voltar no fim do formulário,
visível também no celular. Ele retorna à etapa anterior; na primeira etapa,
retorna ao painel de configuração. Voltar às etapas permanece no topo para
abrir diretamente o painel. Os botões ficam desabilitados durante carregamento
ou salvamento. Voltar não envia formulários nem marca etapas concluídas.

O smoke existente verifica a sequência PDV → usuários → fiscal → empresa → painel,
e a preservação do endereço, em desktop e celular. Publicação em homologação via PR
após CI verde.
