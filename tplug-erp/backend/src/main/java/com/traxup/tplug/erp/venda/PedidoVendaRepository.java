package com.traxup.tplug.erp.venda;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PedidoVendaRepository extends JpaRepository<PedidoVenda, UUID> {
    List<PedidoVenda> findAllByTenantIdOrderByCriadoEmDesc(UUID tenantId);
    Optional<PedidoVenda> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndNumeroIgnoreCase(UUID tenantId, String numero);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PedidoVenda p where p.id = :id and p.tenantId = :tenantId")
    Optional<PedidoVenda> buscarParaFaturar(@Param("id") UUID id, @Param("tenantId") UUID tenantId);
}
