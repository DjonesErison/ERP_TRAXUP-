package com.traxup.tplug.erp.contabilidade;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "contabilidade.sped.worker", name = "enabled",
        havingValue = "true")
@ConditionalOnBean({SpedGeradorPort.class, SpedArquivoStoragePort.class})
public class SpedExportacaoScheduler {
    private final JdbcTemplate jdbc;
    private final SpedExportacaoWorkerApplicationService worker;
    private final int tenantBatch;
    private final int itemBatch;
    private final int maxTentativas;

    public SpedExportacaoScheduler(
            JdbcTemplate jdbc,
            SpedExportacaoWorkerApplicationService worker,
            @Value("${contabilidade.sped.worker.tenant-batch:50}")
            int tenantBatch,
            @Value("${contabilidade.sped.worker.item-batch:5}")
            int itemBatch,
            @Value("${contabilidade.sped.worker.max-tentativas:5}")
            int maxTentativas) {
        this.jdbc = jdbc;
        this.worker = worker;
        this.tenantBatch = normalizarTenantBatch(tenantBatch);
        this.itemBatch = SpedExportacaoWorkerApplicationService
                .normalizarLimite(itemBatch);
        this.maxTentativas = SpedExportacaoWorkerApplicationService
                .normalizarMaxTentativas(maxTentativas);
    }

    @Scheduled(
            initialDelayString =
                    "${contabilidade.sped.worker.initial-delay-ms:45000}",
            fixedDelayString =
                    "${contabilidade.sped.worker.delay-ms:60000}")
    public void processarPendentes() {
        List<UUID> tenants = jdbc.query("""
                SELECT tenant_id
                FROM contabilidade_sped_exportacoes
                WHERE tentativas_processamento < ?
                  AND (
                      status IN ('PENDENTE', 'FALHOU')
                      OR (
                          status = 'PROCESSANDO'
                          AND atualizado_em
                              < CURRENT_TIMESTAMP - INTERVAL '15 minutes'
                      )
                  )
                GROUP BY tenant_id
                ORDER BY min(criado_em)
                LIMIT ?
                """, (rs, n) -> rs.getObject("tenant_id", UUID.class),
                maxTentativas, tenantBatch);
        for (UUID tenantId : tenants) {
            try {
                worker.processar(tenantId, itemBatch);
            } catch (RuntimeException ignorada) {
                // Uma falha de infraestrutura nao bloqueia os demais tenants.
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
