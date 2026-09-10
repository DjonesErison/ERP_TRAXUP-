package com.traxup.tplug.erp.fiscal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "fiscal.storage", name = "enabled",
        havingValue = "true")
public class FiscalArquivoScheduler {
    private final JdbcTemplate jdbc;
    private final FiscalArquivoWorkerApplicationService worker;
    private final int tenantBatch;
    private final int itemBatch;

    public FiscalArquivoScheduler(
            JdbcTemplate jdbc,
            FiscalArquivoWorkerApplicationService worker,
            @Value("${fiscal.storage.worker.tenant-batch:50}") int tenantBatch,
            @Value("${fiscal.storage.worker.item-batch:10}") int itemBatch) {
        this.jdbc = jdbc;
        this.worker = worker;
        this.tenantBatch = normalizarTenantBatch(tenantBatch);
        this.itemBatch = FiscalArquivoWorkerApplicationService
                .normalizarLimite(itemBatch);
    }

    @Scheduled(
            initialDelayString = "${fiscal.storage.worker.initial-delay-ms:30000}",
            fixedDelayString = "${fiscal.storage.worker.delay-ms:60000}")
    public void processarPendentes() {
        List<UUID> tenants = jdbc.query("""
                SELECT tenant_id
                FROM fiscal_arquivos
                WHERE status IN ('PENDENTE', 'FALHOU')
                GROUP BY tenant_id
                ORDER BY min(criado_em)
                LIMIT ?
                """, (rs, n) -> rs.getObject("tenant_id", UUID.class), tenantBatch);
        for (UUID tenantId : tenants) {
            try {
                worker.processar(tenantId, itemBatch);
            } catch (RuntimeException ignorada) {
                // O proximo tenant deve continuar; o worker persiste falhas por arquivo.
            }
        }
    }

    static int normalizarTenantBatch(int limite) {
        if (limite < 1 || limite > 200)
            throw new IllegalArgumentException(
                    "Lote de tenants deve estar entre 1 e 200");
        return limite;
    }
}
