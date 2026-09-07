package com.traxup.tplug.erp.venda;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PedidoVendaRepository extends JpaRepository<PedidoVenda, UUID> {
    List<PedidoVenda> findAllByTenantIdOrderByCriadoEmDescIdAsc(UUID tenantId);
    List<PedidoVenda> findAllByTenantIdOrderByCriadoEmDescIdAsc(UUID tenantId, Pageable pageable);

    @Query("""
            select p from PedidoVenda p
            where p.tenantId = :tenantId
              and (:filialId is null or p.filialId = :filialId)
              and (:clienteId is null or p.clienteId = :clienteId)
              and (:status is null or p.status = :status)
              and (cast(:inicio as instant) is null or p.criadoEm >= :inicio)
              and (cast(:fim as instant) is null or p.criadoEm <= :fim)
            order by p.criadoEm desc, p.id asc
            """)
    List<PedidoVenda> buscarRecentesFiltrados(@Param("tenantId") UUID tenantId,
                                               @Param("filialId") UUID filialId,
                                               @Param("clienteId") UUID clienteId,
                                               @Param("status") String status,
                                               @Param("inicio") Instant inicio,
                                               @Param("fim") Instant fim,
                                               Pageable pageable);

    Optional<PedidoVenda> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndNumeroIgnoreCase(UUID tenantId, String numero);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PedidoVenda p where p.id = :id and p.tenantId = :tenantId")
    Optional<PedidoVenda> buscarParaFaturar(@Param("id") UUID id, @Param("tenantId") UUID tenantId);
}
