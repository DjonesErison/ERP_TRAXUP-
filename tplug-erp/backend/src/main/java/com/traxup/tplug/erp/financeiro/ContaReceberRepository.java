package com.traxup.tplug.erp.financeiro;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContaReceberRepository extends JpaRepository<ContaReceber, UUID> {
    List<ContaReceber> findAllByTenantIdOrderByVencimentoAscCriadoEmDesc(UUID tenantId);
    Optional<ContaReceber> findByIdAndTenantId(UUID id, UUID tenantId);
    List<ContaReceber> findAllByTenantIdAndOrigemTipoAndOrigemIdOrderByVencimentoAscCriadoEmDesc(
            UUID tenantId, String origemTipo, UUID origemId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT conta FROM ContaReceber conta WHERE conta.id = :id AND conta.tenantId = :tenantId")
    Optional<ContaReceber> findByIdAndTenantIdForUpdate(@Param("id") UUID id,
                                                        @Param("tenantId") UUID tenantId);
}
