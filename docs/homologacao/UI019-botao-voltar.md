# UI-019 — Botão Voltar nas configurações

Cada uma das quatro telas possui um botão Voltar no fim do formulário,
visível também no celular. Em empresa, fiscal, usuários/permissões e vendas/PDV,
Voltar abre diretamente o painel principal das configurações, com os quatro cartões.
Voltar às etapas permanece no topo com o mesmo destino.

Os botões ficam desabilitados durante carregamento ou salvamento. Voltar não envia
formulários nem marca etapas concluídas. Não há retorno sequencial entre etapas.

O smoke existente abre cada tela e verifica o retorno direto ao painel,
preservando o endereço da empresa, em desktop e celular.
Publicação em homologação via PR após CI verde.
