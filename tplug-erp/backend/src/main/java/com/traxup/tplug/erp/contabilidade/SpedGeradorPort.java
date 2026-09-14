package com.traxup.tplug.erp.contabilidade;

import java.time.YearMonth;
import java.util.UUID;

public interface SpedGeradorPort {
    Artefato gerar(UUID tenantId, UUID filialId,
                   String tipo, YearMonth competencia);

    default String provedorId() {
        return "";
    }

    record Artefato(
            byte[] conteudo,
            String provedorId,
            String versaoLayout) {}
}
