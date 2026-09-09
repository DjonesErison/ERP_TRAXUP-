package com.traxup.tplug.erp.fiscal;

import java.util.UUID;

public interface FiscalAssinaturaPort {
    Resultado assinar(Comando comando);

    record Comando(UUID documentoId, UUID certificadoId, String thumbprintSha256, String xml) {}

    record Resultado(String conteudoAssinado, String hashSha256, String tipo, String algoritmo) {}
}
