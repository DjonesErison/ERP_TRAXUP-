package com.traxup.tplug.erp.crm;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteFollowUpRepository extends JpaRepository<ClienteFollowUp, UUID> {
    @Query("""
            select f from ClienteFollowUp f
            where f.tenantId = :tenantId
              and (:filialId is null or f.filialId = :filialId)
              and (:clienteId is null or f.clienteId = :clienteId)
              and (:status is null or f.status = :status)
            order by f.agendadoPara asc, f.id asc
            """)
    List<ClienteFollowUp> buscar(@Param("tenantId") UUID tenantId,
                                 @Param("filialId") UUID filialId,
                                 @Param("clienteId") UUID clienteId,
                                 @Param("status") String status,
                                 Pageable pageable);

    Optional<ClienteFollowUp> findByIdAndTenantId(UUID id, UUID tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from ClienteFollowUp f where f.id = :id and f.tenantId = :tenantId")
    Optional<ClienteFollowUp> buscarParaAtualizar(@Param("id") UUID id, @Param("tenantId") UUID tenantId);
}
