package com.traxup.tplug.erp.pdv;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PdvVendaSincronizacaoRepository extends JpaRepository<PdvVendaSincronizacao, UUID> {
    Optional<PdvVendaSincronizacao> findByTenantIdAndTerminalIdAndOperacaoLocalId(UUID tenantId, UUID terminalId, UUID operacaoLocalId);
    boolean existsByTenantIdAndFilialIdAndSerieAndNumeroLocal(UUID tenantId, UUID filialId, Integer serie, Long numeroLocal);
}
