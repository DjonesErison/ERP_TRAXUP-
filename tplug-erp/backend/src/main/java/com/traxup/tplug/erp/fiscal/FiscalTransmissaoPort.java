package com.traxup.tplug.erp.fiscal;

import java.util.UUID;

public interface FiscalTransmissaoPort {
    Resultado transmitir(Comando comando);

    record Comando(UUID documentoId, UUID assinaturaId, String conteudoAssinado,
                   String hashRequisicao) {}

    record Resultado(String provedor, String status, String codigoResposta,
                     String mensagemResposta, String protocolo, String hashResposta) {}
}
