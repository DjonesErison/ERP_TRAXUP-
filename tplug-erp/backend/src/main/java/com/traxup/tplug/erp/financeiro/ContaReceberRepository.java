package com.traxup.tplug.erp.financeiro;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContaReceberRepository extends JpaRepository<ContaReceber, UUID> {
    List<ContaReceber> findAllByTenantIdOrderByVencimentoAscCriadoEmDesc(UUID tenantId);
    Optional<ContaReceber> findByIdAndTenantId(UUID id, UUID tenantId);
    List<ContaReceber> findAllByTenantIdAndOrigemTipoAndOrigemIdOrderByVencimentoAscCriadoEmDesc(
            UUID tenantId, String origemTipo, UUID origemId);
}
