package com.traxup.tplug.erp.inventario;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventarioSessaoRepository extends JpaRepository<InventarioSessao, UUID> {
    Optional<InventarioSessao> findByIdAndTenantId(UUID id, UUID tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InventarioSessao i where i.id = :id and i.tenantId = :tenantId")
    Optional<InventarioSessao> buscarParaAtualizar(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query("""
            select i from InventarioSessao i
            where i.tenantId = :tenantId
              and (:filialId is null or i.filialId = :filialId)
              and (:status is null or i.status = :status)
            order by i.criadoEm desc, i.id asc
            """)
    List<InventarioSessao> buscar(@Param("tenantId") UUID tenantId,
                                   @Param("filialId") UUID filialId,
                                   @Param("status") String status,
                                   Pageable pageable);
}
