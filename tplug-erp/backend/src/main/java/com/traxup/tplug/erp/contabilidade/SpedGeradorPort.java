package com.traxup.tplug.erp.contabilidade;

import java.time.YearMonth;
import java.util.UUID;

public interface SpedGeradorPort {
    Artefato gerar(UUID tenantId, String tipo, YearMonth competencia);

    record Artefato(
            byte[] conteudo,
            String provedorId,
            String versaoLayout) {}
}
