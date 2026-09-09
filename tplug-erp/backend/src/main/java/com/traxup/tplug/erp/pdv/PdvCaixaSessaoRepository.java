package com.traxup.tplug.erp.pdv;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PdvCaixaSessaoRepository extends JpaRepository<PdvCaixaSessao, UUID> {
    Optional<PdvCaixaSessao> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<PdvCaixaSessao> findFirstByTenantIdAndTerminalIdAndStatusOrderByAbertoEmDesc(UUID tenantId, UUID terminalId, String status);
    List<PdvCaixaSessao> findAllByTenantIdAndTerminalIdOrderByAbertoEmDesc(UUID tenantId, UUID terminalId);
}
