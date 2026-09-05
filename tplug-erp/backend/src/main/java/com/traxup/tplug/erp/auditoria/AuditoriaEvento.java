package com.traxup.tplug.erp.auditoria;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditoriaEvento(
        UUID id,
        UUID tenantId,
        UUID usuarioId,
        UUID empresaId,
        UUID filialId,
        String operacao,
        String entidade,
        UUID entidadeId,
        String detalhes,
        OffsetDateTime criadoEm) {
}
