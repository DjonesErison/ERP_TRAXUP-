package com.traxup.tplug.erp.pdv;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PdvConfiguracaoRepository extends JpaRepository<PdvConfiguracao, UUID> {
    Optional<PdvConfiguracao> findByTenantIdAndTerminalId(UUID tenantId, UUID terminalId);
}
